//
//  RoutesUIView.swift
//  Workruta
//
//  Created by The KING on 24/07/2022.
//

import SwiftUI

struct RoutesUIView: View {
    
    let this: RoutesViewController
    let routeId: String
    let myId = UserDefaults.standard.string(forKey: "myId")!
    @State var requesting = true
    @State var imageUrl = URL(string: "")
    @State var uiImage: UIImage = UIImage()
    @State var name: String!
    @State var type: String!
    @State var userId: String!
    @State var pathKey: String!
    @State var routerId: String!
    @State var routeIdTo: String!
    @State var canRespond: Bool!
    @State var notResponded: Bool!
    @State var showOption = false
    @State var showAction = false
    @State var startSearch = false
    @State var showPassengers = false
    @State var actionText = ""
    @State var actionNeg = ""
    @State var actionPos = ""
    @State var actionKey = ""
    @State var optionId = "0"
    @State var dataObj = [String]()
    @State var editingObj = [String: Any]()
    @State var optionsObj = [String: Bool]()
    private let cacheUtil = CacheUtil()
    private let dataIcons: [String] = [
        "location.slash.fill",
        "location.fill",
        "arrow.up.left.and.arrow.down.right",
        "clock",
        "calendar",
        "dollarsign.circle.fill",
        "person.fill.badge.plus",
        "person.3.fill"
    ]
    private let optionKeys: [String] = [
        "viewOwnRoute",
        "viewMergedRoute",
        "searchForRoutes",
        "canPay",
        "canRate",
        "hasPassengers",
        "canStart",
        "canEnd",
        "canEdit",
        "canCancel"
    ]
    private let optionData: [String: [String]] = [
        "viewOwnRoute": ["car.fill", "View Own Route"],
        "viewMergedRoute": ["car.fill", "View Merged Ride"],
        "canPay": ["creditcard", "Make Payment"],
        "canRate": ["star.fill", "Rate This Ride"],
        "hasPassengers": ["person.3.fill", "See Users Following This Ride"],
        "canStart": ["play.fill", "Start Ride"],
        "canEdit": ["square.and.pencil", "Edit Route"],
        "canCancel": ["xmark.circle.fill", "Cancel Route"],
        "searchForRoutes": ["location.fill.viewfinder", "Search for Available Routes"],
        "canEnd": ["stop.fill", "End Ride"]
    ]
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 0) {
                HStack {
                    HStack {
                        Image(systemName: "arrow.left")
                            .foregroundColor(Colors.white)
                            .font(.system(size: 18, weight: .bold))
                        Text("Route Details")
                            .foregroundColor(Colors.white)
                            .font(.system(size: 18, weight: .bold))
                    }
                    .padding(10)
                    .onTapGesture {
                        if !requesting {
                            this.finish()
                        }
                    }
                    Spacer()
                }
                .background(Colors.mainColor)
                ZStack {
                    Colors.white
                    if dataObj.count > 0 {
                        ScrollView{
                            ZStack(alignment: .topLeading) {
                                VStack {
                                    VStack(spacing: 20) {
                                        ForEach(0..<dataObj.count, id: \.self){ keyIndex in
                                            if type == "P" || keyIndex < 5 {
                                                HStack (spacing: 10){
                                                    let icon = dataIcons[keyIndex]
                                                    let text = dataObj[keyIndex]
                                                    Image(systemName: icon)
                                                        .resizable()
                                                        .frame(width: 20, height: 20)
                                                        .foregroundColor(Colors.asher)
                                                    Text(text)
                                                        .foregroundColor(Colors.asher)
                                                        .font(.system(size: 17))
                                                }
                                                .frame(maxWidth: .infinity, alignment: .leading)
                                                .padding(10)
                                                .border(Colors.asher, width: 1.0)
                                                .cornerRadius(7)
                                                .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.ash, lineWidth: 1.0))
                                            }
                                        }
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(EdgeInsets(top: 80, leading: 10, bottom: 20, trailing: 10))
                                    .border(Colors.asher, width: 1.0)
                                    .cornerRadius(7)
                                    .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.ash, lineWidth: 1.0))
                                }
                                .padding(EdgeInsets(top: 10, leading: 20, bottom: 0, trailing: 10))
                                VStack {
                                    HStack {
                                        Text(name!)
                                            .frame(maxWidth: .infinity, alignment: .leading)
                                            .lineLimit(1)
                                            .foregroundColor(Colors.white)
                                            .font(.system(size: 20))
                                            .padding(EdgeInsets(top: 10, leading: 40, bottom: 10, trailing: 10))
                                        
                                    }
                                    .frame(maxWidth: .infinity)
                                    .background(Colors.mainColor)
                                    .clipShape(Trapezium())
                                    .cornerRadius(20)
                                    .overlay(RoundedRectangle(cornerRadius: 20.0).stroke(Colors.mainColor, lineWidth: 0))
                                }
                                .padding(EdgeInsets(top: 0, leading: 40, bottom: 0, trailing: 0))
                                Image(uiImage: uiImage)
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 70, height: 70)
                                    .backgroundImage(imageName: "default_photo")
                                    .clipShape(RoundedRectangle(cornerRadius: 35))
                                    .contentShape(Circle())
                            }
                            .padding(10)
                        }
                        .padding(0)
                        .onTapGesture {
                            showOption = false
                        }
                    }
                    if requesting {
                        ZStack {
                            Colors.whiteFade
                            GIFView(gifName: "loader")
                                .frame(width: 30, height: 30, alignment: .center)
                        }
                    }
                }
                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
            }
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .overlay(
            HStack(spacing: 0) {
                if optionsObj.count > 0 && !requesting {
                    VStack(spacing: 0) {
                        Button  {
                            showOption.toggle()
                        } label: {
                            HStack {
                                Spacer()
                                Image(systemName: "ellipsis")
                                    .foregroundColor(Colors.mainColor)
                                Spacer()
                            }
                            .padding(15)
                        }
                        if showOption {
                            ForEach(0..<optionKeys.count, id: \.self){ index in
                                self.optionView(index)
                            }
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .background(Colors.ash)
                    .animation(
                        Animation.linear(duration: 0.1)
                    )
                    .cornerRadius(10, corners: [.topLeft, .topRight])
                    .overlay(
                        RoundedRectangle(cornerRadius: 0)
                            .stroke(Colors.mainColor, lineWidth: 1.5)
                            .cornerRadius(10, corners: [.topLeft, .topRight])
                    )
                }
            }
                .frame(maxWidth: .infinity)
                .padding(EdgeInsets(top: 0, leading: 30, bottom: -1.5, trailing: 30))
            , alignment: .bottom
        )
        .overlay(
            ZStack{
                if showAction {
                    Colors.blackFade
                    VStack (spacing: 30){
                        Text(actionText)
                        HStack (spacing: 15){
                            Spacer()
                            Button {
                                showAction = false
                                if !requesting && actionKey == "respond" {
                                    executeTask(action: "rejected")
                                }
                            } label: {
                                Text(actionNeg)
                                    .foregroundColor(Colors.white)
                                    .padding(.vertical, 10)
                                    .padding(.horizontal, 20)
                                    .background(Colors.black)
                                    .cornerRadius(5)
                            }
                            Button {
                                showAction = false
                                if !requesting {
                                    let action = actionKey == "respond" ? "accepted" : ""
                                    executeTask(action: action)
                                }
                            } label: {
                                Text(actionPos)
                                    .foregroundColor(Colors.white)
                                    .padding(.vertical, 10)
                                    .padding(.horizontal, 20)
                                    .background(Colors.mainColor)
                                    .cornerRadius(5)
                            }
                        }
                    }
                    .frame(width: UIScreen.main.bounds.width - 100)
                    .padding(20)
                    .background(Colors.white)
                    .cornerRadius(10)
                }
            }
            , alignment: .bottom
        )
        .sheet(isPresented: $startSearch) {
            SearchUIView(previousRoutesUIView: nil, routeSearchUIView: nil, routesUIView: self, thisIndex: 2, routeId: $optionId).background(Colors.black)
        }
        .sheet(isPresented: $showPassengers) {
            ShowPassengers(routesUIView: self, routeId: routeId).background(Colors.black)
        }
        .onAppear(){
            self.getRouteContents()
        }
    }
    
    func getRouteContents(){
        guard let url = URL(string: Constants.routeUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "routeId": routeId
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                self.requesting = false
                if error != nil {
                    print("Error")
                    return
                }
                do {
                    if let data = data {
                        //print("Data: \(String(decoding: data, as: UTF8.self))")
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let photo = object["photo"] as! String
                            self.imageUrl = URL(string: Constants.www + photo)!
                            self.userId = object["userId"] as? String
                            self.name = object["name"] as? String
                            self.routeIdTo = object["routeIdTo"] as? String
                            self.routerId = object["routerId"] as? String
                            self.pathKey = object["pathKey"] as? String
                            self.canRespond = object["canRespond"] as? Bool
                            self.notResponded = object["notResponded"] as? Bool
                            self.type = object["type"] as? String
                            if object["options"] != nil {
                                self.optionsObj = object["options"] as! [String : Bool]
                            }
                            let locationFrom = object["locationFrom"] as! String
                            let locationTo = object["locationTo"] as! String
                            let passengers = object["passengers"] as! String
                            let passNum = object["passNum"] as! String
                            let passN = object["passN"] as! String
                            let routeDate = object["routeDate"] as! String
                            let freeRide = object["freeRide"] as! Bool
                            let latitudeFrom = Double(object["latitudeFrom"] as! String)!
                            let longitudeFrom = Double(object["longitudeFrom"] as! String)!
                            let latitudeTo = Double(object["latitudeTo"] as! String)!
                            let longitudeTo = Double(object["longitudeTo"] as! String)!
                            let distance = Functions.getDistance(lat1: latitudeFrom, lng1: longitudeFrom, lat2: latitudeTo, lng2: longitudeTo)
                            let time = Functions.getRouteTime(lat1: latitudeFrom, lng1: longitudeFrom, lat2: latitudeTo, lng2: longitudeTo)
                            let cost = freeRide ? "Free" : Functions.getTripCost(distance: distance)
                            let dateFormatter = DateFormatter()
                            dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:00"
                            let date = dateFormatter.date(from: routeDate)
                            if self.canRespond && self.notResponded {
                                self.optionsObj["respond"] = true
                            }
                            editingObj["id"] = self.routeId
                            editingObj["type"] = self.type
                            editingObj["locationFrom"] = locationFrom
                            editingObj["locationTo"] = locationTo
                            editingObj["routeDate"] = routeDate
                            editingObj["passNum"] = passN
                            editingObj["freeRide"] = freeRide
                            editingObj["latitudeFrom"] = String(latitudeFrom)
                            editingObj["longitudeFrom"] = String(longitudeFrom)
                            editingObj["latitudeTo"] = String(latitudeTo)
                            editingObj["longitudeTo"] = String(longitudeTo)
                            self.dataObj = [
                                locationFrom,
                                locationTo,
                                "\(distance) mi",
                                time,
                                date!.friendlyString(),
                                cost!,
                                passNum,
                                passengers
                            ]
                            self.getUserImage()
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
            }
        }
        urlSession.resume()
    }
    
    func getUserImage(){
        cacheUtil.getImage(imageURL: self.imageUrl!) { data, error in
            if let data = data {
                uiImage = UIImage(data: data)!
            }
        }
    }
    
    func openRouteInfo(routeId: String){
        startSearch.toggle()
        this.openRouteInfo(routeIdFrom: self.routeId, routeIdTo: routeId)
    }
    
    func listenToOptionClick(_ key: String) {
        showOption = false
        switch key {
            case "viewOwnRoute":
                this.openRouteInfo(key, routeIdFrom: routeId, routeIdTo: routeIdTo)
            case "viewMergedRoute":
                this.openRouteInfo(key, routeIdFrom: routeId, routeIdTo: routeIdTo)
            case "searchForRoutes":
                optionId = routeId
                startSearch.toggle()
            case "hasPassengers":
                showPassengers.toggle()
            case "canEdit":
                editRoute()
            case "canStart":
                actionText = "Do You Want to Start this Ride?"
                actionNeg = "Cancel"
                actionPos = "Start"
                actionKey = key
                showAction = true
            case "canEnd":
                actionText = "Do You Want to End this Ride?"
                actionNeg = "Cancel"
                actionPos = "End"
                actionKey = key
                showAction = true
            case "respond":
                actionText = "Respond To Request"
                actionNeg = "Reject"
                actionPos = "Accept"
                actionKey = key
                showAction = true
            case "canCancel":
                actionText = "Do You want to cancel this Route?"
                actionNeg = "No"
                actionPos = "Yes"
                actionKey = key
                showAction = true
            default:
                return
        }
    }
    
    @ViewBuilder
    func optionView(_ index: Int) -> some View {
        let optionKey = optionKeys[index]
        if optionsObj.containsKey(optionKey){
            let isTrue = optionsObj[optionKey]!
            if (isTrue && optionKey != "canFollow") || (optionKey == "isFollowingRide" && optionsObj["canFollow"]!) {
                let optionIcon = optionData[optionKey]![0]
                let optionText = optionKey == "isFollowingRide" ? isTrue ? "Unfollow Ride" : "Follow Ride" : optionData[optionKey]![1]
                Button {
                    listenToOptionClick(optionKey)
                } label: {
                    HStack(spacing: 0) {
                        VStack(spacing: 0){
                            HStack (spacing: 10){
                                Image(systemName: optionIcon)
                                    .resizable()
                                    .frame(width: 20, height: 20)
                                    .foregroundColor(Colors.mainColor)
                                Text(optionText)
                                    .foregroundColor(Colors.mainColor)
                                    .font(.system(size: 17))
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(10)
                            .border(Colors.asher, width: 1.0)
                            .cornerRadius(7)
                            .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.ash, lineWidth: 1.0))
                        }
                        .background(Colors.white)
                        .cornerRadius(7)
                        .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.ash, lineWidth: 1.0))
                    }
                    .padding(EdgeInsets(top: 0, leading: 5, bottom: 5, trailing: 5))
                }
            }
        }
    }
    
    func editRoute(){
        this.openEditor(that: self, routeData: editingObj)
    }
    
    func endRoute(){
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "routeId": routeId,
            "action": "endRoute"
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                self.requesting = false
                if error != nil {
                    print("Error")
                    return
                }
                do {
                    if let data = data {
                        //print("Data: \(String(decoding: data, as: UTF8.self))")
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as! Bool
                            if noError {
                                self.optionsObj["canEnd"] = false
                                self.this.showAlertBox(title: "", msg: "Ride Ended", btnText: "Close")
                            } else {
                                self.this.showAlertBox(title: "", msg: "An Error Occured", btnText: "Close")
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
    
    func startRoute(){
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "routeId": routeId,
            "action": "startRoute"
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                self.requesting = false
                if error != nil {
                    print("Error")
                    return
                }
                do {
                    if let data = data {
                        //print("Data: \(String(decoding: data, as: UTF8.self))")
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as! Bool
                            if noError {
                                self.optionsObj["canStart"] = false
                                self.optionsObj["canEdit"] = false
                                self.optionsObj["canCancel"] = false
                                self.optionsObj["canEnd"] = true
                                self.this.showAlertBox(title: "", msg: "Ride Started", btnText: "Close")
                            } else {
                                self.this.showAlertBox(title: "", msg: "An Error Occured", btnText: "Close")
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
    
    func respondToRequest(todo: String){
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "userTo": userId,
            "dataId": routeId,
            "extraId": routeIdTo,
            "key": pathKey,
            "todo": todo,
            "action": "followAction"
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                self.requesting = false
                if error != nil {
                    print("Error")
                    return
                }
                do {
                    if let data = data {
                        //print("Data: \(String(decoding: data, as: UTF8.self))")
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as! Bool
                            if noError {
                                self.optionsObj["respond"] = false
                                self.this.showAlertBox(title: "", msg: "Request \(todo)", btnText: "Close")
                            } else {
                                self.this.showAlertBox(title: "", msg: "An Error Occured", btnText: "Close")
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
    
    func cancelRoute(){
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "id": routeId,
            "action": "cancelRoute"
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                self.requesting = false
                if error != nil {
                    print("Error")
                    return
                }
                do {
                    if let data = data {
                        //print("Data: \(String(decoding: data, as: UTF8.self))")
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as! Bool
                            if noError {
                                self.optionsObj = [String: Bool]()
                                self.this.showAlertBox(title: "", msg: "Route Cancelled", btnText: "Close")
                            } else {
                                self.this.showAlertBox(title: "", msg: "An Error Occured", btnText: "Close")
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
    
    func saveEdit(routeData: [String: Any]){
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        self.requesting = true
        var parameters = routeData
        parameters["freeRide"] = String(routeData["freeRide"] as! Bool)
        parameters["user"] = myId
        parameters["action"] = "editRoute"
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                self.requesting = false
                if error != nil {
                    self.this.showAlertBox(title: "", msg: "Sorry, An Error Occured", btnText: "Close")
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as! Bool
                            if !noError {
                                let dataStr = object["dataStr"] as! String
                                self.this.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                                return
                            }
                            self.getRouteContents()
                            self.this.showAlertBox(title: "", msg: "Edit Saved", btnText: "Close")
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
            }
        }
        urlSession.resume()
    }
    
    func executeTask(action: String) {
        switch actionKey {
            case "canStart":
                startRoute()
            case "canEnd":
                endRoute()
            case "respond":
                respondToRequest(todo: action)
            case "canCancel":
                cancelRoute()
            default:
                return
        }
    }
}
