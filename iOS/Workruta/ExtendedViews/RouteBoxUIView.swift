//
//  RouteBoxUIView.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import SwiftUI

struct RouteBoxUIView: View {
    
    @ObservedObject var routeModel: RouteModel
    let colorDictionary = ColorDictionary()
    
    var body: some View {
        let routeDate = routeModel.routeArray["routeDate"] as! String
        let type = routeModel.routeArray["type"] as! String
        let locationFrom = routeModel.routeArray["locationFrom"] as! String
        let locationTo = routeModel.routeArray["locationTo"] as! String
        let status = routeModel.routeArray["status"] as! String
        let editing = routeModel.routeArray["editing"] as! Bool
        let dateArray = routeDate.convertToDateArray()
        let year = String(dateArray[0])
        let mon = Constants.months[dateArray[1] - 1]
        let d = dateArray[2]
        let day = d < 10 ? "0" + String(d) : String(d)
        ZStack {
            HStack{
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
                VStack(alignment: .center, spacing: 10) {
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
                    .frame(width: UIScreen.main.bounds.width - 90, alignment: .leading)
                    HStack {
                    }
                    .frame(width: UIScreen.main.bounds.width - 110, height: 7)
                    .background(colorDictionary.statusBarColors[status])
                    .cornerRadius(3.5)
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
                    .frame(width: UIScreen.main.bounds.width - 90, alignment: .leading)
                }
                .frame(width: UIScreen.main.bounds.width - 90)
            }
            .frame(width: UIScreen.main.bounds.width - 12, height: 70)
            .background(Colors.white)
            .padding(2)
            .cornerRadius(7)
            .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.asher, lineWidth: 1.0))
        }
        .padding(left: 4, top: 2, right: 4, bottom: 2)
        .overlay(
            VStack{
                Text(type)
                    .frame(width: 24, height: 24, alignment: .center)
                    .foregroundColor(Colors.white)
                    .font(.system(size: 18, weight: .bold))
                    .background(Colors.red)
                    .cornerRadius(50)
            }
                .padding(left: 66, top: 28)
            , alignment: .topLeading
        )
        .overlay(
            HStack{
                if editing {
                    ZStack{
                        GIFView(gifName: "loader")
                            .frame(width: 30, height: 30, alignment: .center)
                    }
                        .frame(width: UIScreen.main.bounds.width - 12, height: 75)
                        .background(Colors.whiteFade)
                        .padding(2)
                        .cornerRadius(7)
                        .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.asher, lineWidth: 0.0))
                }
            }
            , alignment: .topLeading
        )
    }
}
