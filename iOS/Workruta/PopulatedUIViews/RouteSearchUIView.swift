//
//  RouteSearchUIView.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import SwiftUI

struct RouteSearchUIView: View {
    
    let this: RouteSearchViewController
    let myId: String!
    @State var routeModels: [[String: Any]] = []
    @State var routeData = [String: Any]()
    @State var fromSetter = false
    @State var toSetter = false
    @State var requesting = false
    @State var fetching = true
    @State var showAction = false
    @State var showSheet = false
    @State var showSearch = false
    @State var showDate = false
    @State var selectedDate = Date()
    @State var initialDate = Date()
    @State var optionId = "0"
    @State var locationFrom = ""
    @State var locationTo = ""
    @State var latitudeFrom = 0.0
    @State var longitudeFrom = 0.0
    @State var latitudeTo = 0.0
    @State var longitudeTo = 0.0
    @State var dateStr = Strings.select_date
    @State var distanceStr = ""
    let maxDate = Date().addingTimeInterval(+(60 * 60 * 24 * 7))
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 20) {
                HStack(spacing: 4) {
                    Text("From:")
                        .frame(width: 45)
                        .foregroundColor(Colors.asher)
                        .font(.system(size: 16))
                        .multilineTextAlignment(.trailing)
                    Text(locationFrom)
                        .frame(width: UIScreen.main.bounds.width - 135, alignment: .leading)
                        .foregroundColor(Colors.black)
                        .font(.system(size: 16))
                        .lineLimit(1)
                    Image(systemName: "magnifyingglass")
                        .resizable()
                        .frame(width: 20, height: 20)
                        .foregroundColor(Colors.asher)
                }
                .frame(width: UIScreen.main.bounds.width - 60)
                .padding(10)
                .border(Colors.mainColor, width: 1)
                .cornerRadius(7.0)
                .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                .onTapGesture {
                    this.openAutoSuggest(that: self, index: 0)
                }
                HStack(spacing: 4) {
                    Text("To:")
                        .frame(width: 45)
                        .foregroundColor(Colors.asher)
                        .font(.system(size: 16))
                        .multilineTextAlignment(.trailing)
                    Text(locationTo)
                        .frame(width: UIScreen.main.bounds.width - 135, alignment: .leading)
                        .foregroundColor(Colors.black)
                        .font(.system(size: 16))
                        .multilineTextAlignment(.leading)
                        .lineLimit(1)
                    Image(systemName: "magnifyingglass")
                        .resizable()
                        .frame(width: 20, height: 20)
                        .foregroundColor(Colors.asher)
                }
                .frame(width: UIScreen.main.bounds.width - 60)
                .padding(10)
                .border(Colors.mainColor, width: 1)
                .cornerRadius(7.0)
                .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                .onTapGesture {
                    this.openAutoSuggest(that: self, index: 1)
                }
                HStack(alignment: .center) {
                    Spacer()
                    if !distanceStr.isEmpty {
                        Image(systemName: "car.fill")
                            .resizable()
                            .frame(width: 15, height: 15)
                            .foregroundColor(Colors.asher)
                        Text(distanceStr)
                            .foregroundColor(Colors.red)
                            .font(.system(size: 16, weight: .bold))
                    }
                }.padding(right: 10)
                if !fetching {
                    ScrollView(showsIndicators: false){
                        VStack{
                            ForEach(0..<routeModels.count, id: \.self){ index in
                                let routeModel = getRouteModel(dict: routeModels[index])
                                Button {
                                    if !requesting{
                                        let editing = routeModel.routeArray["editing"] as! Bool
                                        let status = routeModel.routeArray["status"] as! String
                                        if !editing && status != "pending" {
                                            routeData = routeModel.routeArray
                                            showAction = true
                                        }
                                    }
                                } label: {
                                    RouteBoxUIView(routeModel: routeModel)
                                }
                            }
                        }
                        .padding(bottom: 20)
                    }
                    .padding(bottom: 80)
                } else {
                    HStack{
                        GIFView(gifName: "loader")
                            .frame(width: 30, height: 30, alignment: .center)
                    }
                    .padding(top: 50)
                }
                Spacer()
            }
            .frame(width: UIScreen.main.bounds.width - 60)
            .padding(top: 60)
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .overlay(
            HStack(alignment: .center, spacing: 10){
                (Text(Image(systemName: "chevron.left")) + Text(Strings.take_ride)).foregroundColor(Colors.white).padding(10).font(.system(size: 18)).onTapGesture {
                    this.finish()
                }
                Spacer()
            }
                .background(Colors.mainColor)
            , alignment: .topLeading
        )
        .overlay(
            VStack{
                HStack {
                    Text(dateStr)
                        .foregroundColor(Colors.asher)
                        .padding(10)
                        .border(Colors.asher, width: 1)
                        .cornerRadius(7.0)
                        .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.asher, lineWidth: 1.0))
                        .onTapGesture {
                            showDate = true
                        }
                    Spacer()
                    Button {
                        UIApplication.shared.hideKeyboard(hide: true)
                        if requesting {
                            this.showAlertBox(title: "", msg: "You can't create more than one route at a time", btnText: "Close")
                        } else {
                            self.accessData()
                        }
                    } label: {
                        Text(Strings.create).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                    }.background(Colors.mainColor).border(Colors.white, width: 1).cornerRadius(7.0).overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                }
                .padding(10)
            }
                .frame(width: UIScreen.main.bounds.width)
                .background(Colors.white)
            , alignment: .bottomLeading
        )
        .overlay(
            ZStack{
                if showDate {
                    Colors.blackFade
                    VStack(alignment: .center, spacing: 30) {
                        DatePicker("", selection: $selectedDate, in: Date()...maxDate, displayedComponents: [.date, .hourAndMinute])
                            .datePickerStyle(GraphicalDatePickerStyle())
                            .frame(width: UIScreen.main.bounds.width - 30, height: UIScreen.main.bounds.width - 30)
                            .background(Colors.white)
                            .colorScheme(.light)
                            .cornerRadius(7)
                            .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                        Button {
                            dateStr = selectedDate.friendlyString()
                            showDate = false
                        } label: {
                            Text(Strings.ok).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                        }
                        .background(Colors.mainColor)
                        .border(Colors.white, width: 1)
                        .cornerRadius(7.0)
                        .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.white, lineWidth: 1.0))
                    }
                }
            }
            , alignment: .topLeading
        )
        .overlay(
            ZStack{
                if showAction {
                    Colors.blackFade
                    VStack (spacing: 30){
                        Text(Strings.recreate_route).multilineTextAlignment(.center).font(.system(size: 16)).foregroundColor(Colors.black)
                        HStack (spacing: 15){
                            Spacer()
                            Button {
                                showAction = false
                            } label: {
                                Text(Strings.cancel).foregroundColor(Colors.white).padding(20).background(Colors.black).cornerRadius(5)
                            }
                            Button {
                                showAction = false
                                showSheet.toggle()
                            } label: {
                                Text(Strings.recreate).foregroundColor(Colors.white).padding(20).background(Colors.mainColor).cornerRadius(5)
                            }
                        }
                    }.frame(width: UIScreen.main.bounds.width - 60).padding(20).background(Colors.white).cornerRadius(10)
                }
            }
            , alignment: .topLeading
        )
        .sheet(isPresented: $showSheet) {
            RecreateRouteUIView(previousRoutesUIView: nil, historyUIView: nil, createRouteUIView: nil, routeSearchUIView: self, thisIndex: 3, routeDetails: routeData).background(Colors.white)
        }
        .sheet(isPresented: $showSearch) {
            SearchUIView(previousRoutesUIView: nil, routeSearchUIView: self, routesUIView: nil, thisIndex: 1, routeId: $optionId).background(Colors.black)
        }
        .onAppear(){
            getRoutes()
        }
    }
    
    func getRoutes() {
        guard let url = URL(string: Constants.getRoutesUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String : String] = [
            "user": myId,
            "maxId": "0",
            "excluded": "pending",
            "type": "U"
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                self.fetching = false
                if error != nil {
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        self.routeModels = json!["data"] as! [[String : Any]]
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
            }
        }
        urlSession.resume()
    }
    
    func createRoute(routeArray: [String: Any]){
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        self.requesting = true
        var routeData = routeArray
        self.routeModels.insert(routeData, at: 0)
        self.showAction = true
        self.showAction = false
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
                    this.showAlertBox(title: "", msg: "Sorry, An Error Occured. Please try again", btnText: "Close")
                    self.routeModels.remove(at: 0)
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
                                self.optionId = String(dataStr["id"] as! Int)
                                routeData["id"] = self.optionId
                                routeData["editing"] = false
                                routeData["date"] = dataStr["date"]
                                routeData["passenger"] = "0"
                                self.routeModels[0] = routeData
                                self.showSearch.toggle()
                            } else {
                                let dataStr = object["dataStr"] as! String
                                self.this.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                                self.routeModels.remove(at: 0)
                            }
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
                self.requesting = false
            }
        }
        urlSession.resume()
    }
    
    func checkRecreateData(passNum: String, freeRide: Bool, routeDate: Date) {
        self.showSheet.toggle()
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1){
            let date = Date()
            if date > routeDate {
                this.showAlertBox(title: "", msg: "Your date and time is invalid", btnText: "Close")
                return
            }
            let dateFormatter: DateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:00"
            let newDate = dateFormatter.string(from: routeDate)
            let routeData: [String: Any] = [
                "editing": true,
                "locationFrom": self.routeData["locationFrom"] as! String,
                "locationTo": self.routeData["locationTo"] as! String,
                "latitudeFrom": self.routeData["latitudeFrom"] as! String,
                "longitudeFrom": self.routeData["longitudeFrom"] as! String,
                "latitudeTo": self.routeData["latitudeTo"] as! String,
                "longitudeTo": self.routeData["longitudeTo"] as! String,
                "freeRide": "true",
                "passNum": "0",
                "user": myId!,
                "type": "U",
                "status": "pending",
                "routeDate": newDate
            ]
            createRoute(routeArray: routeData)
        }
    }
    
    func accessData(){
        if locationFrom == "" || locationTo == "" || !(selectedDate > initialDate) {
            this.showAlertBox(title: "", msg: "Please fill in all fields", btnText: "Close")
            return
        }
        let dateFormatter: DateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:00"
        let newDate = dateFormatter.string(from: selectedDate)
        let routeData: [String: Any] = [
            "editing": true,
            "locationFrom": locationFrom,
            "locationTo": locationTo,
            "latitudeFrom": String(latitudeFrom),
            "longitudeFrom": String(longitudeFrom),
            "latitudeTo": String(latitudeTo),
            "longitudeTo": String(longitudeTo),
            "freeRide": "true",
            "passNum": "0",
            "user": myId!,
            "type": "U",
            "status": "pending",
            "routeDate": newDate
        ]
        self.locationFrom = ""
        self.locationTo = ""
        self.distanceStr = ""
        self.selectedDate = initialDate
        self.dateStr = Strings.select_date
        self.createRoute(routeArray: routeData)
    }
    
    func setLocationValues(address: String, latitude: Double, longitude: Double, index: Int) {
        if index == 0 {
            fromSetter = true
            locationFrom = address
            latitudeFrom = latitude
            longitudeFrom = longitude
        }
        if index == 1 {
            toSetter = true
            locationTo = address
            latitudeTo = latitude
            longitudeTo = longitude
        }
        getDistance()
    }
    
    func getDistance() {
        if toSetter && fromSetter {
            let distance = Functions.getDistance(lat1: latitudeFrom, lng1: longitudeFrom, lat2: latitudeTo, lng2: longitudeTo)
            distanceStr = String(distance) + "mi"
        }
    }
    
    func getRouteModel (dict: [String: Any]) -> RouteModel{
        let routeModel = RouteModel()
        routeModel.routeArray = dict
        return routeModel
    }
    
    func openRouteInfo(routeId: String){
        showSearch.toggle()
        this.openRouteInfo(routeIdFrom: optionId, routeIdTo: routeId)
    }
}
