//
//  Functions.swift
//  Workruta
//
//  Created by The KING on 09/06/2022.
//
import Swift
import Foundation
import UIKit
import CoreLocation

class Functions {
    
    public static func formatPhoneNumber(phoneNumber: String) -> String {
        var phone = "+1 "
        phone += phoneNumber.subString(bounds: 0...2) + " "
        phone += phoneNumber.subString(bounds: 3...5) + " "
        phone += phoneNumber.subString(bounds: 6...9)
        return phone
    }
    
    public func loginUser(parameters: [String: Any]){
        UserDefaults.standard.set(parameters["myId"], forKey: "myId")
        UserDefaults.standard.set(parameters["name"], forKey: "name")
        UserDefaults.standard.set(parameters["email"], forKey: "email")
        UserDefaults.standard.set(parameters["pictured"], forKey: "pictured")
        UserDefaults.standard.set(parameters["photo"], forKey: "photo")
        UserDefaults.standard.set(parameters["phoneVerified"], forKey: "phoneVerified")
        UserDefaults.standard.set(parameters["loggedIn"], forKey: "loggedIn")
        UserDefaults.standard.set(parameters["phoneSet"], forKey: "phoneSet")
        UserDefaults.standard.set(parameters["phoneNumber"], forKey: "phoneNumber")
    }
    
    public func removeAllUserCaches(){
        UserDefaults.standard.removeObject(forKey: "myId")
        UserDefaults.standard.removeObject(forKey: "name")
        UserDefaults.standard.removeObject(forKey: "email")
        UserDefaults.standard.removeObject(forKey: "pictured")
        UserDefaults.standard.removeObject(forKey: "photo")
        UserDefaults.standard.removeObject(forKey: "phoneVerified")
        UserDefaults.standard.removeObject(forKey: "loggedIn")
        UserDefaults.standard.removeObject(forKey: "phoneSet")
        UserDefaults.standard.removeObject(forKey: "expTime")
        UserDefaults.standard.removeObject(forKey: "phoneNumber")
    }
    
    public static func getTimerStr(counter: Int) -> String {
        let mins: Int = counter / 60
        let secs = counter % 60
        var minStr = String(mins)
        var secStr = String(secs)
        if mins < 10 {
            minStr = "0" + minStr
        }
        if secs < 10 {
            secStr = "0" + secStr
        }
        let timeStr = minStr + ":" + secStr
        return timeStr
    }
    
    public static func getDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double) -> Double {
        let coordinateOne = CLLocation(latitude: lat1, longitude: lng1)
        let coordinateTwo = CLLocation(latitude: lat2, longitude: lng2)
        let meters = coordinateOne.distance(from: coordinateTwo)
        let distance = meters / 1609
        return distance.roundUp(to: 2)
    }
    
    public static func getRouteTime(lat1: Double, lng1: Double, lat2: Double, lng2: Double) -> String {
        let distance = getDistance(lat1: lat1, lng1: lng1, lat2: lat2, lng2: lng2) * 60 * 60
        let speed = 50.00
        let fraction = Int(distance / speed)
        let mn = Int(fraction / 60)
        let hr = Int(mn / 60)
        let min = mn % 60
        let h = hr.stringDouble()
        let m = min.stringDouble()
        let time = h + "hr(s) and " + m + "min(s)"
        return time
    }
    
    public static func convertDate(_ s: String) -> String {
        let array = s.split(separator: "-")
        let year = array[0]
        let day = array[2]
        let m = Int(array[1])! - 1
        let month = Constants.months[m]
        let dateStr = day + " " + month + " " + year
        return dateStr
    }
    
    public func convertTextToDictionary(text: String?) -> [String: String]? {
        if text != nil {
            if let data = text!.data(using: .utf8){
                do {
                    return try JSONSerialization.jsonObject(with: data, options: []) as? [String: String]
                } catch {
                    
                }
            }
        }
        return [String: String]()
    }
    
    public func convertDictionaryToText(dictionary: [String: String]?) -> String? {
        if let jsonData = try? JSONEncoder().encode(dictionary){
            return String(data: jsonData, encoding: .utf8)!
        }
        print("Cannot convert dictionary to text")
        return nil
    }
    
    public static func getTripCost(distance: Double) -> String? {
        let ratio = 0.02
        let cents = distance / ratio
        let dollars = cents / 100
        let dollar = max(dollars.roundUp(to: 2), 0.5)
        return "$" + String(dollar)
    }
    
    public static func getConversationId(userFrom: String, userTo: String) -> String {
        let uF = Int(userFrom)!
        let uT = Int(userTo)!
        var conversationId = userFrom + Strings.randStr + userTo
        if uF > uT {
            conversationId = userTo + Strings.randStr + userFrom
        }
        return conversationId
    }
    
    public static func getStripeCost(distance: Double) -> String? {
        let ratio = 0.02
        let cents = distance / ratio
        let dollars = cents / 100
        let dollar = max(dollars.roundUp(to: 2), 0.5)
        let stripeCost = dollar * 100
        return String(format: "%.0f", stripeCost)
    }
    
    public static func toNearest(_ value: Double, _ nearest: Double) -> Double {
        return round(value / nearest) * nearest
    }
    
}
