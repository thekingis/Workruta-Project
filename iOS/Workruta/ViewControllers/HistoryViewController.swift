//
//  HistoryViewController.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import UIKit
import SwiftUI

class HistoryViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    private var routeModels: RouteModels!
    private var myId: String!
    private var maxId = "0"

    override func viewDidLoad() {
        super.viewDidLoad()
        routeModels = RouteModels()
        myId = UserDefaults.standard.string(forKey: "myId")
        
        if controlView != nil {
            let childView = UIHostingController(rootView: HistoryUIView(this: self, routeModels: routeModels))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
            
            getRoutes()
        }

    }
    
    func getRoutes() {
        if !self.routeModels.allLoaded && !self.routeModels.requesting {
            guard let url = URL(string: Constants.getRoutesUrl) else {
                print("URL not found")
                return
            }
            self.routeModels.requesting = true
            let parameters = [
                "user": myId!,
                "maxId": maxId,
                "excluded": "pending"
            ] as [String : Any]
            let datas = parameters.toQueryString
            
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.httpBody = datas.data(using: .utf8)!
            request.addValue("application/json", forHTTPHeaderField: "Accept")
            
            let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
                DispatchQueue.main.async {
                    self.routeModels.requesting = false
                    if error != nil {
                        return
                    }
                    do {
                        if let data = data {
                            let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                            self.maxId = json!["maxId"] as! String
                            self.routeModels.allLoaded = json!["allLoaded"] as! Bool
                            let dataArray = json!["data"] as! [[String : Any]]
                            self.routeModels.routesArray.append(contentsOf: dataArray)
                        }
                    } catch let myJSONError {
                        print(myJSONError)
                    }
                }
            }
            urlSession.resume()
        }
    }
    
    func openRouteInfo(routeId: String){
        let storyboard = UIStoryboard(name: "Extras", bundle: nil)
        let viewController  = storyboard.instantiateViewController(identifier: "RoutesView") as RoutesViewController
        viewController.routeId = routeId
        viewController.modalPresentationStyle = .fullScreen
        self.present(viewController, animated: true, completion: nil)
    }
    
    func recreateRoute(passNum: String, freeRide: Bool, routeDate: Date, routeArray: [String: Any]) {
        let date = Date()
        if date > routeDate {
            self.showAlertBox(title: "", msg: "Your date and time is invalid", btnText: "Close")
            return
        }
        if passNum == "" {
            self.showAlertBox(title: "", msg: "Please specify the number of passengers allowed", btnText: "Close")
            return
        }
        if Int(passNum) == 0 {
            self.showAlertBox(title: "", msg: "You must allow at least one passenger", btnText: "Close")
            return
        }
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        let dateFormatter: DateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:00"
        let newDate = dateFormatter.string(from: routeDate)
        var routeData = routeArray
        routeData["id"] = "0"
        routeData["editing"] = true
        routeData["freeRide"] = String(freeRide)
        routeData["passNum"] = passNum
        routeData["routeDate"] = newDate
        routeData["status"] = "pending"
        self.routeModels.routesArray.insert(routeData, at: 0)
        var parameters = routeData
        parameters["action"] = "createRoute"
        parameters["user"] = myId!
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                if error != nil {
                    self.showAlertBox(title: "", msg: "Sorry, An Error Occured. Please try again", btnText: "Close")
                    self.routeModels.routesArray.remove(at: 0)
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as? Bool
                            routeData["editing"] = false
                            if noError! {
                                let dataStr = (object["dataStr"] as? NSDictionary)!
                                routeData["id"] = String(dataStr["id"] as! Int)
                                routeData["date"] = dataStr["date"]
                                routeData["passenger"] = "0"
                                self.routeModels.routesArray[0] = routeData
                            }
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
            }
        }
        urlSession.resume()
    }

}
