//
//  PreviousRoutesViewController.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import UIKit
import SwiftUI

class PreviousRoutesViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    private var routeModels: RouteModels!
    private var myId: String!
    private var maxId = "0"

    override func viewDidLoad() {
        super.viewDidLoad()
        routeModels = RouteModels()
        myId = UserDefaults.standard.string(forKey: "myId")
        
        if controlView != nil {
            let childView = UIHostingController(rootView: PreviousRoutesUIView(this: self, routeModels: routeModels))
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
                "excluded": "cancel,success"
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
    
    func openRouteInfo(index: Int, routeIndex: Int, routeIdFrom: String, routeIdTo: String){
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            let storyboard = UIStoryboard(name: "Extras", bundle: nil)
            if index == 0 {
                let viewController  = storyboard.instantiateViewController(identifier: "RoutesView") as RoutesViewController
                viewController.routeId = routeIdFrom
                viewController.modalPresentationStyle = .fullScreen
                self.present(viewController, animated: true, completion: nil)
            }
            if index == 2 {
                let viewController  = storyboard.instantiateViewController(identifier: "RouteView") as RouteViewController
                viewController.routeIdFrom = routeIdFrom
                viewController.routeIdTo = routeIdTo
                viewController.this = self
                viewController.routeIndex = routeIndex
                viewController.modalPresentationStyle = .fullScreen
                self.present(viewController, animated: true, completion: nil)
            }
        }
    }
    
    func saveEdit(routeData: [String: Any], routeIndex: Int){
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        let oldArray = self.routeModels.routesArray[routeIndex]
        var array = routeData
        array["editing"] = true
        self.routeModels.routesArray[routeIndex] = array
        var parameters = routeData
        parameters["freeRide"] = String(routeData["freeRide"] as! Bool)
        parameters["user"] = myId!
        parameters["action"] = "editRoute"
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                if error != nil {
                    self.showAlertBox(title: "", msg: "Sorry, An Error Occured", btnText: "Close")
                    array["editing"] = false
                    self.routeModels.routesArray[routeIndex] = array
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as? Bool
                            array["editing"] = false
                            if !noError! {
                                array = oldArray
                                let dataStr = (object["dataStr"] as? String)!
                                self.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                            }
                            self.routeModels.routesArray[routeIndex] = array
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
            }
        }
        urlSession.resume()
    }
    
    func excludeFromRide(routeIndex: Int){
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        var array = self.routeModels.routesArray[routeIndex]
        array["editing"] = true
        self.routeModels.routesArray[routeIndex] = array
        let parameters: [String: String] = [
            "action": "exclude",
            "user": myId!,
            "routerId": array["routerId"] as! String,
            "routeId": array["routeId"] as! String,
            "userTo": array["user"] as! String,
            "pathKey": array["pathKey"] as! String
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                if error != nil {
                    self.showAlertBox(title: "", msg: "Sorry, An Error Occured", btnText: "Close")
                    array["editing"] = false
                    self.routeModels.routesArray[routeIndex] = array
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as? Bool
                            array["editing"] = false
                            if noError! {
                                array["isActive"] = false
                                array["routerId"] = "0"
                                array["routeId"] = "0"
                                array["userTo"] = "0"
                                array["pathKey"] = ""
                                self.showAlertBox(title: "", msg: "You have been excluded as a passenger to this ride", btnText: "Close")
                            } else {
                                let dataStr = (object["dataStr"] as? String)!
                                self.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                            }
                            self.routeModels.routesArray[routeIndex] = array
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
            }
        }
        urlSession.resume()
    }
    
    func openEditor(routeData: [String: Any], routeIndex: Int){
        let storyboard = UIStoryboard(name: "Extras", bundle: nil)
        let viewController  = storyboard.instantiateViewController(identifier: "EditRouteView") as EditRouteViewController
        viewController.routeData = routeData
        viewController.routeIndex = routeIndex
        viewController.this = self
        viewController.modalPresentationStyle = .fullScreen
        self.present(viewController, animated: true, completion: nil)
    }
    
    func cancelRoute(id: String, index: Int) {
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        var array = self.routeModels.routesArray[index]
        array["editing"] = true
        self.routeModels.routesArray[index] = array
        let parameters = [
            "user": myId!,
            "id": id,
            "action": "cancelRoute"
        ] as [String : Any]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                if error != nil {
                    self.showAlertBox(title: "", msg: "Sorry, An Error Occured", btnText: "Close")
                    array["editing"] = false
                    self.routeModels.routesArray[index] = array
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as? Bool
                            if noError! {
                                self.showAlertBox(title: "", msg: "Route Cancelled", btnText: "Close")
                                self.routeModels.routesArray.remove(at: index)
                            } else {
                                array["editing"] = false
                                let dataStr = (object["dataStr"] as? String)!
                                self.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                                self.routeModels.routesArray[index] = array
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
    
    func setRouteActive(routeIndex: Int, routerId: String, routeId: String, userTo: String, pathKey: String, isActive: Bool){
        var array = self.routeModels.routesArray[routeIndex]
        array["isActive"] = isActive
        array["routerId"] = routerId
        array["routeId"] = routeId
        array["userTo"] = userTo
        array["pathKey"] = pathKey
        self.routeModels.routesArray[routeIndex] = array
    }

}
