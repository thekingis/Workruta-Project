//
//  SearchRouteUIView.swift
//  Workruta
//
//  Created by The KING on 20/06/2022.
//

import SwiftUI

struct SearchRouteUIView: View {
    
    let previousRoutesUIView: PreviousRoutesUIView!
    let routeSearchUIView: RouteSearchUIView!
    let routesUIView: RoutesUIView!
    let thisIndex: Int
    let searchData: [String: Any]
    let coordinates: [[String]]!
    let directly: Bool
    @State var uiImage: UIImage = UIImage()
    private let cacheUtil = CacheUtil()
    
    var body: some View {
        let fromLat = Double(coordinates[0][0])!
        let fromLng = Double(coordinates[0][1])!
        let id = searchData["id"] as! String
        let phone = Functions.formatPhoneNumber(phoneNumber: searchData["phone"] as! String)
        let name = searchData["name"] as! String
        let status = searchData["status"] as! String
        let routeDate = searchData["routeDate"] as! String
        let locationFrom = searchData["locationFrom"] as! String
        let locationTo = searchData["locationTo"] as! String
        let latitudeFrom = Double(searchData["latitudeFrom"] as! String)!
        let longitudeFrom = Double(searchData["longitudeFrom"] as! String)!
        let latitudeTo = Double(searchData["latitudeTo"] as! String)!
        let longitudeTo = Double(searchData["longitudeTo"] as! String)!
        let freeRide = searchData["freeRide"] as! Bool
        let dateArray = routeDate.convertToDateArray()
        let year = String(dateArray[0])
        let mon = Constants.months[dateArray[1] - 1]
        let d = dateArray[2]
        let day = d < 10 ? "0" + String(d) : String(d)
        let distAway = Functions.getDistance(lat1: latitudeFrom, lng1: longitudeFrom, lat2: fromLat, lng2: fromLng)
        let distance = Functions.getDistance(lat1: latitudeFrom, lng1: longitudeFrom, lat2: latitudeTo, lng2: longitudeTo)
        let cost = directly ? status : freeRide ? "Free" : Functions.getTripCost(distance: distance)!
        let away = String(distAway) + "mi from you"
        ZStack(alignment: .top){
            VStack {
                HStack {
                    VStack(alignment: .leading) {
                        HStack{
                            Image(systemName: "phone.circle.fill")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 20, height: 20)
                                .foregroundColor(Colors.asher)
                            Text(phone)
                                .foregroundColor(Colors.black)
                                .font(.system(size: 14))
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(left: 50, bottom: 15)
                        HStack(alignment: .center, spacing: 10) {
                            Image(systemName: "location.fill")
                                .resizable()
                                .frame(width: 20, height: 20)
                                .foregroundColor(Colors.blue)
                            Text(locationFrom)
                                .foregroundColor(Colors.black)
                                .font(.system(size: 15))
                                .lineLimit(1)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        HStack(alignment: .center, spacing: 10) {
                            Image(systemName: "location.fill")
                                .resizable()
                                .frame(width: 20, height: 20)
                                .foregroundColor(Colors.green)
                            Text(locationTo)
                                .foregroundColor(Colors.black)
                                .font(.system(size: 15))
                                .lineLimit(1)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .frame(width: UIScreen.main.bounds.width - 170)
                    VStack{
                        if !directly {
                            VStack(alignment: .center) {
                                Text(mon)
                                    .foregroundColor(Colors.white)
                                    .font(.system(size: 15, weight: .bold))
                                Text(day)
                                    .foregroundColor(Colors.white)
                                    .font(.system(size: 35, weight: .bold))
                                    .padding(-15)
                                Text(year)
                                    .foregroundColor(Colors.white)
                                    .font(.system(size: 16))
                            }
                            .frame(width: 70, height: 70)
                            .background(Colors.orange)
                            .cornerRadius(7)
                        }
                        VStack(alignment: .leading) {
                            Text(cost)
                                .frame(width: directly ? 80 : 50, height: 20)
                                .background(directly ? status == "accepted" ? Colors.green : Colors.yellow : freeRide ? Colors.green : Colors.normalRed)
                                .foregroundColor(Colors.white)
                                .font(.system(size: 15))
                        }
                        .padding(EdgeInsets(top: -5, leading: directly ? -10 : -30, bottom: 0, trailing: 0))
                    }
                }
                .frame(width: UIScreen.main.bounds.width - 80, height: 125)
                .border(Colors.asher, width: 1.0)
                .cornerRadius(7)
                .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.asher, lineWidth: 1.0))
            }
            .padding(top: 30)
            VStack {
                HStack {
                    Text(name)
                        .frame(width: UIScreen.main.bounds.width - 120, alignment: .leading)
                        .lineLimit(1)
                        .foregroundColor(Colors.white)
                        .font(.system(size: 20))
                        .padding(left: 60, top: 10, right: 10, bottom: 10)
                    
                }
                .frame(width: UIScreen.main.bounds.width - 60)
                .background(Colors.mainColor)
                .clipShape(Trapezium())
                .cornerRadius(20)
                .overlay(RoundedRectangle(cornerRadius: 20.0).stroke(Colors.mainColor, lineWidth: 0))
            }
            .padding(left: 30)
        }
        .frame(width: UIScreen.main.bounds.width - 40)
        .overlay(
            Image(uiImage: uiImage)
                .resizable()
                .scaledToFill()
                .frame(width: 70, height: 70)
                .backgroundImage(imageName: "default_photo")
                .clipShape(RoundedRectangle(cornerRadius: 35))
                .contentShape(Circle())
            , alignment: .topLeading
        )
        .overlay(
            VStack {
                HStack {
                    Text(away)
                        .foregroundColor(Colors.asher)
                        .font(.system(size: 15))
                }
                .frame(width: UIScreen.main.bounds.width - 155, height: 25)
                .background(Colors.white)
                .border(Colors.asher, width: 1.0)
                .cornerRadius(7)
                .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.asher, lineWidth: 1.0))
            }
                .padding(-10)
            , alignment: .bottom
        )
        .overlay(
            VStack {
                ZStack {
                    Image(systemName: "arrow.right")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 40, height: 40)
                        .foregroundColor(Colors.white)
                }
                .frame(width: 60, height: 60)
                .background(Colors.mainColor)
                .clipShape(RoundedRectangle(cornerRadius: 30))
                .contentShape(Circle())
                .onTapGesture {
                    if thisIndex == 0 {
                        previousRoutesUIView.openRouteInfo(routeId: id)
                    } else if thisIndex == 1 {
                        routeSearchUIView.openRouteInfo(routeId: id)
                    } else if thisIndex == 2 {
                        routesUIView.openRouteInfo(routeId: id)
                    } else if thisIndex == 3 {
                        routesUIView.showPassengers.toggle()
                        routesUIView.this.openRouteInfo("viewOwnRoute", routeIdFrom: "", routeIdTo: id)
                    }
                }
            }
                .padding(-10)
            , alignment: .bottomTrailing
        )
        .onAppear(){
            getUserImage()
        }
    }
    
    func getUserImage(){
        let photo = searchData["photo"] as! String
        let imageUrl = URL(string: Constants.www + photo)!
        cacheUtil.getImage(imageURL: imageUrl) { data, error in
            if let data = data {
                uiImage = UIImage(data: data)!
            }
        }
    }
}
