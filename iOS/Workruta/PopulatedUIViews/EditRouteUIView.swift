//
//  EditRouteUIView.swift
//  Workruta
//
//  Created by The KING on 19/06/2022.
//

import SwiftUI

struct EditRouteUIView: View {
    
    let this: EditRouteViewController
    @State var routeData: [String: Any]
    @State var showAction = false
    @State var showDate = false
    @State var selectedDate = Date()
    @State var freeRide = false
    @State var locationFrom = ""
    @State var locationTo = ""
    @State var passNum = ""
    @State var dateStr = ""
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
                if !distanceStr.isEmpty {
                    HStack(alignment: .center) {
                        Spacer()
                        Image(systemName: "car.fill")
                            .resizable()
                            .frame(width: 15, height: 15)
                            .foregroundColor(Colors.asher)
                        Text(distanceStr)
                            .foregroundColor(Colors.red)
                            .font(.system(size: 16, weight: .bold))
                    }.padding(right: 10)
                }
                Spacer()
            }
            .frame(width: UIScreen.main.bounds.width - 60)
            .padding(top: 60)
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .onAppear(perform: {
            initializeVars()
        })
        .overlay(
            HStack(alignment: .center, spacing: 10){
                (Text(Image(systemName: "chevron.left")) + Text(Strings.edit_route)).foregroundColor(Colors.white).padding(10).font(.system(size: 18)).onTapGesture {
                    showAction = true
                }
                Spacer()
            }
                .background(Colors.mainColor)
            , alignment: .topLeading
        )
        .overlay(
            VStack{
                VStack(spacing: 20){
                    if routeData["type"] as! String == "P" {
                        HStack(alignment: .center, spacing: 20){
                            Text(Strings.number_of_passengers_allowed)
                                .foregroundColor(Colors.black)
                            TextField("", text: $passNum){
                                UIApplication.shared.hideKeyboard(hide: false)
                            }
                            .frame(width: 50)
                            .multilineTextAlignment(.center)
                            .background(Colors.white)
                            .foregroundColor(Colors.black)
                            .font(.system(size: 17))
                            .padding(10)
                            .cornerRadius(6)
                            .overlay(RoundedRectangle(cornerRadius: 6.0).stroke(Colors.asher, lineWidth: 1.0))
                            .keyboardType(UIKeyboardType.numberPad)
                            Spacer()
                        }
                        HStack(alignment: .center){
                            CheckBoxView(checked: $freeRide)
                                .frame(width: 30, height: 30)
                                .padding(EdgeInsets(top: 0, leading: 0, bottom: 0, trailing: -15))
                            Text(Strings.make_this_ride_free)
                                .foregroundColor(Colors.black)
                                .font(.system(size: 16))
                                .onTapGesture {
                                    freeRide.toggle()
                                }
                            Spacer()
                        }
                    }
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
                            stabilizeObject()
                            this.editRoute(routeData: routeData)
                        } label: {
                            Text(Strings.save).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                        }.background(Colors.mainColor).border(Colors.white, width: 1).cornerRadius(7.0).overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                    }
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
                        Text(Strings.discard_text).multilineTextAlignment(.center).font(.system(size: 16)).foregroundColor(Colors.black)
                        HStack (spacing: 15){
                            Spacer()
                            Button {
                                showAction = false
                            } label: {
                                Text(Strings.cancel).foregroundColor(Colors.white).padding(20).background(Colors.black).cornerRadius(5)
                            }
                            Button {
                                this.finish()
                            } label: {
                                Text(Strings.discard).foregroundColor(Colors.white).padding(20).background(Colors.mainColor).cornerRadius(5)
                            }
                        }
                    }.frame(width: UIScreen.main.bounds.width - 60).padding(20).background(Colors.white).cornerRadius(10)
                }
            }
            , alignment: .topLeading
        )
    }
    
    func initializeVars(){
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        selectedDate = dateFormatter.date(from: routeData["routeDate"] as! String)!
        dateStr = selectedDate.friendlyString()
        locationFrom = routeData["locationFrom"] as! String
        locationTo = routeData["locationTo"] as! String
        passNum = routeData["passNum"] as! String
        freeRide = routeData["freeRide"] as! Bool
        getDistance()
    }
    
    func stabilizeObject() {
        let dateStr = selectedDate.dateTimeStamp()
        routeData["passNum"] = passNum
        routeData["freeRide"] = freeRide
        routeData["routeDate"] = dateStr
    }
    
    func setLocationValues(address: String, latitude: Double, longitude: Double, index: Int) {
        let lat = String(latitude)
        let lng = String(longitude)
        if index == 0 {
            locationFrom = address
            routeData["locationFrom"] = address
            routeData["latitudeFrom"] = lat
            routeData["longitudeFrom"] = lng
        }
        if index == 1 {
            locationTo = address
            routeData["locationTo"] = address
            routeData["latitudeTo"] = lat
            routeData["longitudeTo"] = lng
        }
        getDistance()
    }
    
    func getDistance() {
        let lat1 = Double(routeData["latitudeFrom"] as! String)!
        let lng1 = Double(routeData["longitudeFrom"] as! String)!
        let lat2 = Double(routeData["latitudeTo"] as! String)!
        let lng2 = Double(routeData["longitudeTo"] as! String)!
        let distance = Functions.getDistance(lat1: lat1, lng1: lng1, lat2: lat2, lng2: lng2)
        distanceStr = String(distance) + "mi"
    }
    
}
