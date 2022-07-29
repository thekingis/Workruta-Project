//
//  RouteUIView.swift
//  Workruta
//
//  Created by The KING on 19/06/2022.
//

import Stripe
import SwiftUI

struct RouteUIView: View {
    
    let this: RouteViewController
    let that: PreviousRoutesViewController!
    let routeIdTo: String
    let routeIdFrom: String
    let myId: String
    let routeIndex: Int!
    let myName = UserDefaults.standard.string(forKey: "name")!
    @State var imageUrl = URL(string: "")
    @State var uiImage: UIImage = UIImage()
    @State var name: String!
    @State var userEmail: String!
    @State var pathKey: String!
    @State var userId = "0"
    @State var routerId: String!
    @State var status: String!
    @State var paymentId: String!
    @State var distance: Double!
    @State var requesting = true
    @State var showOption = false
    @State var showCupertino = false
    @State var showRater = false
    @State var showPaymentSheet = false
    @State var notRated: Bool!
    @State var notStarted: Bool!
    @State var followingRide: Bool!
    @State var availableData: Bool!
    @State var rating: Double = 0
    @State var showCheckmark = -30
    @State var dataObj = [String: Any]()
    @State var optionsObj = [String: Bool]()
    @State var paymentSheet: PaymentSheet!
    private let cacheUtil = CacheUtil()
    private let dataKeys: [String] = [
        "phone",
        "away",
        "locationFrom",
        "locationTo",
        "distance",
        "time",
        "cost",
        "passNum",
        "passengers",
        "routeDate"
    ]
    private let optionKeys: [String] = [
        "viewOwnRoute",
        "canFollow",
        "canPay",
        "canRate",
        "hasPassengers",
        "startDate",
        "canEdit",
        "canCancel"
    ]
    private let dataIcons: [String: String] = [
        "phone": "phone",
        "away": "location.fill.viewfinder",
        "locationFrom": "location.slash.fill",
        "locationTo": "location.fill",
        "distance": "arrow.up.left.and.arrow.down.right",
        "time": "clock",
        "cost": "dollarsign.circle.fill",
        "passNum": "person.fill.badge.plus",
        "passengers": "person.3.fill",
        "routeDate": "calendar"
    ]
    private let optionData: [String: [String]] = [
        "viewOwnRoute": ["car.fill", "View Own Route"],
        "canFollow": ["dot.radiowaves.up.forward", ""],
        "canPay": ["creditcard", "Make Payment"],
        "canRate": ["star.fill", "Rate This Ride"],
        "hasPassengers": ["person.3.fill", "See Users Following This Ride"],
        "startDate": ["play.fill", "Start Ride"],
        "canEdit": ["square.and.pencil", "Edit Route"],
        "canCancel": ["xmark.circle.fill", "Cancel Route"]
    ]
    
    var body: some View {
        let c = dataKeys.count
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
                                        if followingRide {
                                            Text(status == "pending" ? "Request Pending Approval" : status == "rejected" ? "Request Rejected" : "Request Accepted")
                                                .foregroundColor(Colors.black)
                                                .font(.system(size: 17))
                                                .frame(maxWidth: .infinity, alignment: .leading)
                                                .padding(10)
                                                .background(status == "pending" ? Colors.shadeYellow : status == "rejected" ? Colors.shadeRed : Colors.shadeGreen)
                                                .overlay(RoundedRectangle(cornerRadius: 4.0).stroke(status == "pending" ? Colors.deepYellow : status == "rejected" ? Colors.deepRed : Colors.deepGreen, lineWidth: 1.0))
                                            
                                        }
                                        ForEach(0..<c, id: \.self){ keyIndex in
                                            HStack (spacing: 10){
                                                let key = dataKeys[keyIndex]
                                                let text = getText(key: key)
                                                Image(systemName: dataIcons[key]!)
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
                                        HStack (spacing: 20) {
                                            Button {
                                                showOption = false
                                                this.openPage(index: 0, userId: userId, name: name, userEmail: userEmail, imageUrl: imageUrl!)
                                            } label: {
                                                Text(Strings.send_message)
                                                    .frame(maxWidth: .infinity)
                                                    .foregroundColor(Colors.asher)
                                                    .padding(10)
                                                    .border(Colors.asher, width: 1.0)
                                                    .cornerRadius(7)
                                                    .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.ash, lineWidth: 1.0))
                                            }
                                            Button {
                                                showOption = false
                                                this.openPage(index: 1, userId: userId, name: name, userEmail: userEmail, imageUrl: imageUrl!)
                                            } label: {
                                                Text(Strings.view_profile)
                                                    .frame(maxWidth: .infinity)
                                                    .foregroundColor(Colors.asher)
                                                    .padding(10)
                                                    .border(Colors.asher, width: 1.0)
                                                    .cornerRadius(7)
                                                    .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.ash, lineWidth: 1.0))
                                            }
                                        }
                                        .padding(10)
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
                if showPaymentSheet {
                    Colors.blackFade
                    PaymentSheet.PaymentButton(paymentSheet: paymentSheet, onCompletion: onPaymentCompletion) {
                        HStack{
                            Text("Click to Continue")
                                .foregroundColor(Colors.white)
                                .font(.system(size: 20))
                                .padding(.vertical, 10)
                                .padding(.horizontal, 20)
                                .cornerRadius(5)
                        }
                        .background(Colors.mainColor)
                        .cornerRadius(5)
                    }
                }
            }
            , alignment: .bottom
        )
        .overlay(
            ZStack{
                if showRater {
                    Colors.blackFade
                    VStack (spacing: 30){
                        RatingBar(rating: $rating, maxRating: 5)
                        HStack (spacing: 15){
                            Spacer()
                            Button {
                                if !requesting {
                                    rating = 0
                                    showRater = false
                                }
                            } label: {
                                Text(Strings.cancel)
                                    .foregroundColor(Colors.white)
                                    .padding(.vertical, 10)
                                    .padding(.horizontal, 20)
                                    .background(Colors.black)
                                    .cornerRadius(5)
                            }
                            Button {
                                if !requesting {
                                    rateRide()
                                }
                            } label: {
                                if showCupertino {
                                    if showCheckmark < 0 {
                                        GIFView(gifName: "cupertino")
                                            .frame(width: 30, height: 30, alignment: .center)
                                            .padding(.horizontal, 20)
                                    } else {
                                        Image(systemName: "checkmark")
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 30, height: 30, alignment: .center)
                                            .padding(.horizontal, 20)
                                            .foregroundColor(Colors.green)
                                            .clipShape(Rectangle().offset(x: CGFloat(showCheckmark)))
                                            .animation(
                                                Animation.interpolatingSpring(stiffness: 170, damping: 15)
                                            )
                                    }
                                } else {
                                    Text("Rate")
                                        .foregroundColor(Colors.white)
                                        .padding(.vertical, 10)
                                        .padding(.horizontal, 20)
                                        .background(Colors.mainColor)
                                        .cornerRadius(5)
                                }
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
        .onAppear(){
            self.getRouteContents()
        }
    }
    
    func rateRide() {
        if rating == 0 {
            this.showAlertBox(title: "", msg: "Please make a rating", btnText: "Close")
            return
        }
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        self.showCupertino = true
        self.requesting = true
        let parameters: [String: String] = [
            "routeId": routeIdTo,
            "rating": String(rating),
            "user": myId,
            "userTo": userId,
            "action": "rateRide",
            "routerId": routerId
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                if error != nil {
                    self.requesting = false
                    print("Error")
                    return
                }
                self.showCheckmark = 0
                self.notRated = false
                self.optionsObj["canRate"] = false
                DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                    self.requesting = false
                    self.showRater = false
                }
            }
        }
        urlSession.resume()
    }
    
    func makePayment() {
        guard let url = URL(string: Constants.stripeAPIUrl) else {
            print("URL not found")
            return
        }
        self.requesting = true
        let stripeCost = Functions.getStripeCost(distance: distance)!
        let parameters: [String: String] = [
            "routeIdTo": routeIdTo,
            "routeIdFrom": routeIdFrom,
            "userFrom": myId,
            "userTo": userId,
            "amount": stripeCost,
            "routerId": routerId
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
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
                                let clientSecretKey = object["clientSecretKey"] as! String
                                let ephemeralKey = object["ephemeralKey"] as! String
                                let customerId = object["customerId"] as! String
                                self.paymentId = object["paymentId"] as? String
                                STPAPIClient.shared.publishableKey = Constants.stripePKTestAPIKey
                                var configuration = PaymentSheet.Configuration()
                                configuration.merchantDisplayName = Strings.app_name
                                configuration.customer = .init(id: customerId, ephemeralKeySecret: ephemeralKey)
                                // Set `allowsDelayedPaymentMethods` to true if your business can handle payment methods
                                // that complete payment after a delay, like SEPA Debit and Sofort.
                                //configuration.allowsDelayedPaymentMethods = true
                                self.paymentSheet = PaymentSheet(paymentIntentClientSecret: clientSecretKey, configuration: configuration)
                                self.showPaymentSheet = true
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
    
    func onPaymentCompletion(result: PaymentSheetResult) {
        showPaymentSheet = false
        switch result {
            case .completed:
                verifyPayment()
            case .failed(_):
                this.showAlertBox(title: "", msg: "Failed to complete payment. Please try again", btnText: "Close")
            case .canceled:
                return
        }
    }
    
    func verifyPayment(){
        showPaymentSheet = false
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        self.requesting = true
        let stripeCost = Functions.getStripeCost(distance: distance)!
        let parameters: [String: String] = [
            "routeId": routeIdTo,
            "paymentId": paymentId,
            "user": myId,
            "userTo": userId,
            "amount": stripeCost,
            "action": "confirmPayment",
            "name": myName
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                if error != nil {
                    print("Error")
                    return
                }
                self.optionsObj["canPay"] = false
                self.this.showAlertBox(title: "", msg: "Payment Successful", btnText: "Close")
                if notRated {
                    showRater = true
                }
                self.requesting = false
            }
        }
        urlSession.resume()
    }
    
    func followRoute() {
        guard let url = URL(string: Constants.followRouteUrl) else {
            print("URL not found")
            return
        }
        self.requesting = true
        let parameters: [String: String] = [
            "routeIdTo": routeIdTo,
            "routeIdFrom": routeIdFrom,
            "toFollow": String(!followingRide),
            "userFrom": myId,
            "userTo": userId,
            "pathKey": pathKey,
            "routerId": routerId
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                if error != nil {
                    print("Error")
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as! Bool
                            if noError {
                                self.followingRide = !self.followingRide
                                self.status = self.followingRide ? "pending" : ""
                                self.pathKey = object["pathKey"] as? String
                                self.routerId = object["routerId"] as? String
                                self.optionsObj["isFollowingRide"] = self.followingRide
                                let cluded = self.followingRide ? "included" : "excluded"
                                let text = "You have been " + cluded + " as a passenger to this ride"
                                if self.that != nil {
                                    self.that.setRouteActive(
                                        routeIndex: self.routeIndex,
                                        routerId: self.routerId,
                                        routeId: self.routeIdTo,
                                        userTo: self.userId,
                                        pathKey: self.pathKey,
                                        isActive: self.followingRide
                                    )
                                }
                                self.this.showAlertBox(title: "", msg: text, btnText: "Close")
                            } else {
                                let errorMsg = object["errorMsg"] as! String
                                self.this.showAlertBox(title: "", msg: errorMsg, btnText: "Close")
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
    
    func getRouteContents(){
        guard let url = URL(string: Constants.getRouteUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "routeIdTo": routeIdTo,
            "routeIdFrom": routeIdFrom
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
                            let toLatitudeFrom = Double(object["toLatitudeFrom"] as! String)!
                            let toLongitudeFrom = Double(object["toLongitudeFrom"] as! String)!
                            let toLatitudeTo = Double(object["toLatitudeTo"] as! String)!
                            let toLongitudeTo = Double(object["toLongitudeTo"] as! String)!
                            let photo = object["photo"] as! String
                            self.imageUrl = URL(string: Constants.www + photo)!
                            self.userId = (object["userTo"] as? String)!
                            self.name = object["name"] as? String
                            self.userEmail = object["email"] as? String
                            self.routerId = object["routerId"] as? String
                            self.pathKey = object["pathKey"] as? String
                            self.status = object["status"] as? String
                            self.notRated = object["notRated"] as? Bool
                            self.notStarted = object["notStarted"] as? Bool
                            self.optionsObj = object["options"] as! [String : Bool]
                            self.dataObj = object as! [String : Any]
                            self.followingRide = self.status != nil &&  !self.status.isEmpty
                            self.distance = Functions.getDistance(lat1: toLatitudeFrom, lng1: toLongitudeFrom, lat2: toLatitudeTo, lng2: toLongitudeTo)
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
    
    func getText(key: String) -> String {
        var text = ""
        let fromLatitudeFrom = Double(dataObj["fromLatitudeFrom"] as! String)!
        let fromLongitudeFrom = Double(dataObj["fromLongitudeFrom"] as! String)!
        let toLatitudeFrom = Double(dataObj["toLatitudeFrom"] as! String)!
        let toLongitudeFrom = Double(dataObj["toLongitudeFrom"] as! String)!
        let toLatitudeTo = Double(dataObj["toLatitudeTo"] as! String)!
        let toLongitudeTo = Double(dataObj["toLongitudeTo"] as! String)!
        switch key {
            case "phone":
                text = Functions.formatPhoneNumber(phoneNumber: dataObj["phone"] as! String)
            case "away":
                let distance = Functions.getDistance(lat1: fromLatitudeFrom, lng1: fromLongitudeFrom, lat2: toLatitudeFrom, lng2: toLongitudeFrom)
                text = String(distance) + "mi away from you"
            case "locationFrom":
                text = dataObj["locationFrom"] as! String
            case "locationTo":
                text = dataObj["locationTo"] as! String
            case "distance":
                text = String(distance) + "mi"
            case "time":
                text = Functions.getRouteTime(lat1: toLatitudeFrom, lng1: toLongitudeFrom, lat2: toLatitudeTo, lng2: toLongitudeTo)
            case "cost":
                let freeRide = dataObj["freeRide"] as! Bool
                let distance = Functions.getDistance(lat1: toLatitudeFrom, lng1: toLongitudeFrom, lat2: toLatitudeTo, lng2: toLongitudeTo)
                text = (freeRide ? "Free" : Functions.getTripCost(distance: distance))!
            case "passNum":
                text = dataObj["passNum"] as! String
            case "passengers":
                text = dataObj["passengers"] as! String
            case "routeDate":
                let routeDate = dataObj["routeDate"] as! String
                let dateFormatter = DateFormatter()
                dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:00"
                let date = dateFormatter.date(from: routeDate)
                text = (date?.friendlyString())!
            default:
                text = ""
        }
        return text
    }
    
    func listenToOptionClick(_ key: String) {
        showOption = false
        switch key {
            case "viewOwnRoute":
                this.openRoute()
            case "canFollow":
                followRoute()
            case "canPay":
                makePayment()
            case "canRate":
                showRater = true
            default:
                return
        }
    }
    
    @ViewBuilder
    func optionView(_ index: Int) -> some View {
        let optionKey = optionKeys[index]
        if optionsObj.containsKey(optionKey){
            let isTrue = optionsObj[optionKey]!
            if isTrue {
                let optionIcon = optionData[optionKey]![0]
                let optionText = optionKey == "canFollow" ? optionsObj["isFollowingRide"]! ? "Unfollow Ride" : "Follow Ride" : optionData[optionKey]![1]
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
}
