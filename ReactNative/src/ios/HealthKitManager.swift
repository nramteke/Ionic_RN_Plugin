import Foundation
import HealthKit

@objc(HealthKitManager)
public class HealthKitManager:RCTEventEmitter {
    static let sharedInstance = HealthKitManager()
    let apiBaseURL = "https://scblife.api.activelife.io/"
    var lastSync = 0.0
    
    @objc static func getSharedInstance() -> HealthKitManager {return sharedInstance}
    
    @objc open override func supportedEvents() -> [String] {return ["Uploading","TimeOut","Offline"]}
    
    @objc(setCredentials:userID:accessToken:)
    func setCredentials(insurerID:String,userID:String,accessToken:String)->Void {
        print(">>> setUserCredentials: \(insurerID) \(userID) \(accessToken)")
        UserDefaults.standard.set(insurerID,forKey:"insurerID")
        UserDefaults.standard.set(userID,forKey:"userID")
        UserDefaults.standard.set(accessToken,forKey:"accessToken")
        authorizeHealthKit()
    }
    
    func authorizeHealthKit() {
        print(">>> authorizeHealthKit")
        let healthKitTypesToRead:Set = [
            HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.stepCount)!,
            HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!,
            HKObjectType.workoutType()
        ]
        HKHealthStore().requestAuthorization(toShare:nil,read:healthKitTypesToRead) {(success,error) in self.startObservingHealthKitStore()}
    }
    
    @objc func startObservingHealthKitStore() {
        let stepCount = HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.stepCount)
        HKHealthStore().execute(HKObserverQuery(sampleType:stepCount!,predicate:nil,updateHandler:newAllDayTrackingAvailable))
        HKHealthStore().enableBackgroundDelivery(for:stepCount!,frequency:.immediate,withCompletion:{(succeeded,error) in
            if succeeded {
                print(">>> Enabled background delivery of HealthKit store changes for stepCount")
            } else {
                if let error = error {
                    print(">>> Failed to enable background delivery of HealthKit store changes for stepCount: \(error)")
                }
            }
        })
        let workoutType = HKObjectType.workoutType()
        HKHealthStore().execute(HKObserverQuery(sampleType:workoutType,predicate:nil,updateHandler:newAllDayTrackingAvailable))
        HKHealthStore().enableBackgroundDelivery(for:workoutType,frequency:.immediate,withCompletion:{(succeeded,error) in
            if succeeded {
                print(">>> Enabled background delivery of HealthKit store changes for workoutType")
            } else {
                if let error = error {
                    print(">>> Failed to enable background delivery of HealthKit store changes for workoutType: \(error)")
                }
            }
        })
    }
    
    func newAllDayTrackingAvailable(query:HKObserverQuery,completionHandler:@escaping HKObserverQueryCompletionHandler,error:Error?) {
        if Date().timeIntervalSinceReferenceDate-lastSync < 10 {
            print(">>> newAllDayTrackingAvailable skip")
            completionHandler()
            return
        }
        lastSync = Date().timeIntervalSinceReferenceDate
        if self.bridge != nil {self.sendEvent(withName:"Uploading",body:nil)}
        print(">>> newAllDayTrackingAvailable")
        var daysToUpload = 1
        if let lastUpload = UserDefaults.standard.object(forKey:"lastUpload") as? Date {
            let calendar = Calendar(identifier:.gregorian)
            let startDate = calendar.startOfDay(for:lastUpload)
            let endDate = calendar.startOfDay(for:Date())
            daysToUpload = calendar.dateComponents([.day],from:startDate,to:endDate).day!+1
        }
        for daysAgo1 in 0..<daysToUpload {
            queryAllDayTracking(metric:"calories",daysAgo:daysAgo1) {daysAgo2,date,calories,dataSourcesCalories in
                self.queryAllDayTracking(metric:"steps",daysAgo:daysAgo2) {daysAgo3,date,steps,dataSourcesSteps in
                    self.queryWorkouts(daysAgo:daysAgo3) {workouts in
                        var calories = calories
                        var steps = steps
                        if !self.hasNonZeroValue(dataset:calories) {calories = []}
                        if !self.hasNonZeroValue(dataset:steps) {steps = []}
                        let dataSources = dataSourcesCalories.union(dataSourcesSteps)
                        self.uploadActivityData(date:date,perMinuteCalories:calories,perMinuteSteps:steps,workouts:workouts,dataSources:Array(dataSources)) {
                            print(">>> newAllDayTrackingAvailable completed")
                            completionHandler()
                        }
                    }
                }
            }
        }
    }
    
  func queryAllDayTracking(metric:String,daysAgo:Int,completion:@escaping(Int,String,[Int],Set<String>)->Void) {
        print(">>> queryAllDayTracking \(metric)")
        var unit:HKUnit!
        var quantityType:HKQuantityType!
        if metric == "steps" {
            unit = HKUnit.count()
            quantityType = HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.stepCount)
        }
        if metric == "calories" {
            unit = HKUnit.kilocalorie()
            quantityType = HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)
        }
        var startDate = Calendar(identifier:.gregorian).startOfDay(for:Date())
        var dateComponent = DateComponents()
        dateComponent.day = -daysAgo
        startDate = Calendar(identifier:.gregorian).date(byAdding:dateComponent,to:startDate)!
        dateComponent.day = 1
        dateComponent.second = -1
        let endDate = Calendar(identifier:.gregorian).date(byAdding:dateComponent,to:startDate)!
        let datePredicate = HKQuery.predicateForSamples(withStart:startDate,end:endDate,options:.strictStartDate)
        let manualPredicate = NSPredicate(format:"metadata.%K != YES",HKMetadataKeyWasUserEntered)
        let compoundPredicate = NSCompoundPredicate(type:.and,subpredicates:[datePredicate,manualPredicate])
        var interval = DateComponents()
        interval.minute = 1
        let query = HKStatisticsCollectionQuery(quantityType:quantityType,quantitySamplePredicate:compoundPredicate,options:[.cumulativeSum,.separateBySource],anchorDate:startDate as Date,intervalComponents:interval)
        query.initialResultsHandler = {query,results,error in
            if let queryError = error as NSError? {
                if queryError.domain == "com.apple.healthkit" && queryError.code == 5 {
                    UserDefaults.standard.removeObject(forKey:"lastSync")
                    print(">>> queryError")
                    self.lastSync = 0.0
                }
            }
            if let results = results {
                var dataset = Array(repeating:0,count:1440)
                var dataSources = Set<String>()
                results.enumerateStatistics(from:startDate,to:endDate) {statistics,stop in
                    let startDateHour = Calendar(identifier:.gregorian).component(.hour,from:statistics.startDate)
                    let startDateMinute = Calendar(identifier:.gregorian).component(.minute,from:statistics.startDate)
                    let minuteOfDay = startDateHour*60+startDateMinute
                    if statistics.sources != nil && statistics.sources!.count != 0 {
                        var values = [0]
                        for source in statistics.sources! {
                            var dataSource = ""
                            if source.bundleIdentifier.starts(with:"com.apple.health") {dataSource = "apple_health"}
                            if source.bundleIdentifier == "com.endomondo.Endomondo" {dataSource = "endomondo"}
                            if source.bundleIdentifier == "com.pacer.pacerapp" {dataSource = "pacer"}
                            if source.bundleIdentifier == "HM.wristband" {dataSource = "mi_fit"}
                            if dataSource == "" {continue}
                            if let quantity = statistics.sumQuantity(for:source) {
                                if metric == "steps" {values.append(Int(quantity.doubleValue(for:unit)))}
                                if metric == "calories" {values.append(Int(quantity.doubleValue(for:unit)*100))}
                                dataSources.insert(dataSource)
                            }
                        }
                        dataset[minuteOfDay] = values.max()!
                    } else if let quantity = statistics.sumQuantity() {
                        if metric == "steps" {dataset[minuteOfDay] = Int(quantity.doubleValue(for:unit))}
                        if metric == "calories" {dataset[minuteOfDay] = Int(quantity.doubleValue(for:unit)*100)}
                        dataSources.insert("apple_health")
                    }
                }
                let formatter = DateFormatter()
                formatter.dateFormat = "yyyyMMdd"
                formatter.calendar = Calendar(identifier:.gregorian)
                let date = formatter.string(from:startDate)
                completion(daysAgo,date,dataset,dataSources)
            }
        }
        HKHealthStore().execute(query)
    }
    
    func queryWorkouts(daysAgo:Int,completion:@escaping([Workout])->Void) {
        print(">>> queryWorkouts")
        var startDate = Calendar(identifier:.gregorian).startOfDay(for:Date())
        var dateComponent = DateComponents()
        dateComponent.day = -daysAgo
        startDate = Calendar(identifier:.gregorian).date(byAdding:dateComponent,to:startDate)!
        dateComponent.day = 1
        dateComponent.second = -1
        let endDate = Calendar(identifier:.gregorian).date(byAdding:dateComponent,to:startDate)!
        let datePredicate = HKQuery.predicateForSamples(withStart:startDate,end:endDate,options:.strictStartDate)
        let manualPredicate = NSPredicate(format:"metadata.%K != YES",HKMetadataKeyWasUserEntered)
        let compoundPredicate = NSCompoundPredicate(type:.and,subpredicates:[datePredicate,manualPredicate])
        let query = HKSampleQuery(sampleType:HKObjectType.workoutType(),predicate:compoundPredicate,limit:0,sortDescriptors:nil,resultsHandler:{(query,samples,error) in
            if let rawWorkouts = samples as? [HKWorkout] {
                var workouts = [Workout]()
                for rawWorkout in rawWorkouts {
                    let year = Calendar(identifier:.gregorian).component(.year,from:rawWorkout.startDate)
                    let month = Calendar(identifier:.gregorian).component(.month,from:rawWorkout.startDate)
                    let day = Calendar(identifier:.gregorian).component(.day,from:rawWorkout.startDate)
                    var zeroPaddingForMonth = ""
                    if month < 10 {zeroPaddingForMonth = "0"}
                    var zeroPaddingForDay = ""
                    if day < 10 {zeroPaddingForDay = "0"}
                    let date = "\(year)\(zeroPaddingForMonth)\(month)\(zeroPaddingForDay)\(day)"
                    let startTimeHour = Calendar(identifier:.gregorian).component(.hour,from:rawWorkout.startDate)
                    let startTimeMinute = Calendar(identifier:.gregorian).component(.minute,from:rawWorkout.startDate)
                    let startTime = startTimeHour*60+startTimeMinute
                    let endTimeHour = Calendar(identifier:.gregorian).component(.hour,from:rawWorkout.endDate)
                    let endTimeMinute = Calendar(identifier:.gregorian).component(.minute,from:rawWorkout.endDate)
                    let endTime = endTimeHour*60+endTimeMinute
                    var workout = Workout(
                        type:self.getWorkoutType(type:rawWorkout.workoutActivityType),
                        bundle_identifier:rawWorkout.source.bundleIdentifier,
                        start_time:startTime,
                        end_time:endTime,
                        date:date,
                        calories:nil,
                        distance:nil,
                        duration:nil
                    )
                    if rawWorkout.source.bundleIdentifier == "de.komoot.berlinbikeapp" {
                        if rawWorkout.totalDistance == nil {continue}
                        workout.distance = Int(rawWorkout.totalDistance!.doubleValue(for:HKUnit.meter()))
                        workout.duration = Int(rawWorkout.duration)
                    } else {
                        if rawWorkout.totalEnergyBurned == nil {continue}
                        workout.calories = Int(rawWorkout.totalEnergyBurned!.doubleValue(for:HKUnit.kilocalorie()))
                    }
                    workouts.append(workout)
                }
                completion(workouts)
                print(">>> workouts: \(workouts)")
            }
        })
        HKHealthStore().execute(query)
    }
    
    func uploadActivityData(date:String,perMinuteCalories:[Int],perMinuteSteps:[Int],workouts:[Workout],dataSources:[String],completion:@escaping()->Void) {
        print(">>> uploadActivityData: \(dataSources) | \(date)")
        let activityData = ActivityData(
            date:date,
            per_minute_calories:perMinuteCalories,
            per_minute_steps:perMinuteSteps,
            workouts:workouts,
            data_sources:dataSources,
            data_source:"apple_health"
        )
        var request = URLRequest(url:URL(string:apiBaseURL+"activity_data_upload")!)
        request.httpMethod = "POST"
        request.allHTTPHeaderFields = getHeader()
        do {request.httpBody = try JSONEncoder().encode(activityData)}
        catch {print(error)}
        URLSession(configuration:URLSessionConfiguration.default).dataTask(with:request){(data,response,error) in
            if let httpError = error as NSError? {
                if httpError.domain == "NSURLErrorDomain" && httpError.code == -1009 {
                    if self.bridge != nil {self.sendEvent(withName:"Offline",body:nil)}
                    print(">>> Offline")
                }
                if httpError.domain == "NSURLErrorDomain" && httpError.code == -1001 {
                    if self.bridge != nil {self.sendEvent(withName:"TimeOut",body:nil)}
                    print(">>> TimeOut")
                }
                self.lastSync = 0.0
            }
            if let httpResponse = response as? HTTPURLResponse {
                print(">>> uploadActivityData statusCode: \(httpResponse.statusCode)")
                if (httpResponse.statusCode == 200) {
                    UserDefaults.standard.set(Date(),forKey:"lastUpload")
                }
            }
            completion()
            }.resume()
    }
    
    @objc(uploadPushToken:)
    func uploadPushToken(pushToken:String) {
        print(">>> pushToken: \(pushToken)")
        var pushToken = pushToken
        pushToken = pushToken.replacingOccurrences(of:"<",with:"")
        pushToken = pushToken.replacingOccurrences(of:">",with:"")
        pushToken = pushToken.replacingOccurrences(of:" ",with:"")
        if let storedPushToken = UserDefaults.standard.string(forKey:"pushToken") {
            if storedPushToken == pushToken {return}
        }
        var request = URLRequest(url:URL(string:self.apiBaseURL+"profile")!)
        request.httpMethod = "POST"
        request.allHTTPHeaderFields = getHeader()
        let profile = Profile(push_token:pushToken,platform:"ios")
        do {request.httpBody = try JSONEncoder().encode(profile)}
        catch {print(error)}
        URLSession(configuration:URLSessionConfiguration.default).dataTask(with:request){(data,response,error) in
            if let httpResponse = response as? HTTPURLResponse {
                print(">>> uploadPushToken statusCode: \(httpResponse.statusCode)")
                if httpResponse.statusCode == 200 {
                    UserDefaults.standard.set(pushToken,forKey:"pushToken")
                }
            }
            }.resume()
    }
    
    struct ActivityData:Codable {
        let date:String
        let per_minute_calories:[Int]
        let per_minute_steps:[Int]
        let workouts:[Workout]
        let data_sources:[String]
        let data_source:String
    }
    
    struct Workout:Codable {
        let type:String
        let bundle_identifier:String
        let start_time:Int
        let end_time:Int
        let date:String
        var calories:Int?
        var distance:Int?
        var duration:Int?
    }
    
    struct Profile:Codable {
        let push_token:String
        let platform:String
    }
    
    func getHeader()->[String:String] {
        return [
            "Insurer-ID":UserDefaults.standard.string(forKey:"insurerID")!,
            "User-ID":UserDefaults.standard.string(forKey:"userID")!,
            "Access-Token":UserDefaults.standard.string(forKey:"accessToken")!,
            "Timezone-Offset":String(-TimeZone.current.secondsFromGMT()/60),
            "Timezone-ID":TimeZone.current.identifier,
            "Language":Locale.current.languageCode!,
            "Version":Bundle.main.infoDictionary!["CFBundleShortVersionString"] as! String,
            "Build":Bundle.main.infoDictionary!["CFBundleVersion"] as! String,
            "Content-Type":"application/json"
        ]
    }
    
    func hasNonZeroValue(dataset:[Int])->Bool {
        var hasNonZeroValue = false
        for value in dataset {
            if value != 0 {
                hasNonZeroValue = true
            }
        }
        return hasNonZeroValue
    }
    
    func getWorkoutType(type:HKWorkoutActivityType)->String {
        switch type {
        case .americanFootball:
            return "americanFootball"
        case .archery:
            return "archery"
        case .australianFootball:
            return "australianFootball"
        case .badminton:
            return "badminton"
        case .baseball:
            return "baseball"
        case .basketball:
            return "basketball"
        case .bowling:
            return "bowling"
        case .boxing:
            return "boxing"
        case .climbing:
            return "climbing"
        case .cricket:
            return "cricket"
        case .crossTraining:
            return "crossTraining"
        case .curling:
            return "curling"
        case .cycling:
            return "cycling"
        case .dance:
            return "dance"
        case .danceInspiredTraining:
            return "danceInspiredTraining"
        case .elliptical:
            return "elliptical"
        case .equestrianSports:
            return "equestrianSports"
        case .fencing:
            return "fencing"
        case .fishing:
            return "fishing"
        case .functionalStrengthTraining:
            return "functionalStrengthTraining"
        case .golf:
            return "golf"
        case .gymnastics:
            return "gymnastics"
        case .handball:
            return "handball"
        case .hiking:
            return "hiking"
        case .hockey:
            return "hockey"
        case .hunting:
            return "hunting"
        case .lacrosse:
            return "lacrosse"
        case .martialArts:
            return "martialArts"
        case .mindAndBody:
            return "mindAndBody"
        case .mixedMetabolicCardioTraining:
            return "mixedMetabolicCardioTraining"
        case .paddleSports:
            return "paddleSports"
        case .play:
            return "play"
        case .preparationAndRecovery:
            return "preparationAndRecovery"
        case .racquetball:
            return "racquetball"
        case .rowing:
            return "rowing"
        case .rugby:
            return "rugby"
        case .running:
            return "running"
        case .sailing:
            return "sailing"
        case .skatingSports:
            return "skatingSports"
        case .snowSports:
            return "snowSports"
        case .soccer:
            return "soccer"
        case .softball:
            return "softball"
        case .squash:
            return "squash"
        case .stairClimbing:
            return "stairClimbing"
        case .surfingSports:
            return "surfingSports"
        case .swimming:
            return "swimming"
        case .tableTennis:
            return "tableTennis"
        case .tennis:
            return "tennis"
        case .trackAndField:
            return "trackAndField"
        case .traditionalStrengthTraining:
            return "traditionalStrengthTraining"
        case .volleyball:
            return "volleyball"
        case .walking:
            return "walking"
        case .waterFitness:
            return "waterFitness"
        case .waterPolo:
            return "waterPolo"
        case .waterSports:
            return "waterSports"
        case .wrestling:
            return "wrestling"
        case .yoga:
            return "yoga"
        case .barre:
            return "barre"
        case .coreTraining:
            return "coreTraining"
        case .crossCountrySkiing:
            return "crossCountrySkiing"
        case .downhillSkiing:
            return "downhillSkiing"
        case .flexibility:
            return "flexibility"
        case .highIntensityIntervalTraining:
            return "highIntensityIntervalTraining"
        case .jumpRope:
            return "jumpRope"
        case .kickboxing:
            return "kickboxing"
        case .pilates:
            return "pilates"
        case .snowboarding:
            return "snowboarding"
        case .stairs:
            return "stairs"
        case .stepTraining:
            return "stepTraining"
        case .wheelchairWalkPace:
            return "wheelchairWalkPace"
        case .wheelchairRunPace:
            return "wheelchairRunPace"
        case .taiChi:
            return "taiChi"
        case .mixedCardio:
            return "mixedCardio"
        case .handCycling:
            return "handCycling"
        default:
            return "other"
        }
    }
}
