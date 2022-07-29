//
//  RecreateRouteUIView.swift
//  Workruta
//
//  Created by The KING on 18/06/2022.
//

import SwiftUI

struct RecreateRouteUIView: View {
    
    let previousRoutesUIView: PreviousRoutesUIView!
    let historyUIView: HistoryUIView!
    let createRouteUIView: CreateRouteUIView!
    let routeSearchUIView: RouteSearchUIView!
    let thisIndex: Int
    let routeDetails: [String: Any]
    @State private var passNum = ""
    @State private var selectedDate = Date()
    @State private var checked = false
    let maxDate = Date().addingTimeInterval(+(60 * 60 * 24 * 7))
    
    var body: some View {
        ZStack{
            Colors.blackFade
            VStack{
                DatePicker("", selection: $selectedDate, in: Date()...maxDate, displayedComponents: [.date, .hourAndMinute])
                    .datePickerStyle(GraphicalDatePickerStyle())
                    .frame(width: UIScreen.main.bounds.width - 30, height: UIScreen.main.bounds.width - 30)
                    .background(Colors.white)
                    .colorScheme(.light)
                    .cornerRadius(7)
                    .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
            }
            .padding(EdgeInsets(top: -100, leading: 0, bottom: 0, trailing: 0))
        }
        .overlay(
            VStack{
                VStack(spacing: 20){
                    if routeDetails["type"] as! String == "P" {
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
                    }
                    HStack(alignment: .center){
                        if routeDetails["type"] as! String == "P" {
                            CheckBoxView(checked: $checked)
                                .frame(width: 30, height: 30)
                                .padding(EdgeInsets(top: 0, leading: 0, bottom: 0, trailing: -15))
                            Text(Strings.make_this_ride_free)
                                .foregroundColor(Colors.black)
                                .font(.system(size: 16))
                                .onTapGesture {
                                    checked.toggle()
                                }
                        }
                        Spacer()
                        Button {
                            UIApplication.shared.hideKeyboard(hide: true)
                            if thisIndex == 1 {
                                historyUIView.checkRecreateData(passNum: passNum, freeRide: checked, routeDate: selectedDate)
                            } else if thisIndex == 2 {
                                createRouteUIView.checkRecreateData(passNum: passNum, freeRide: checked, routeDate: selectedDate)
                            } else if thisIndex == 3 {
                                routeSearchUIView.checkRecreateData(passNum: passNum, freeRide: checked, routeDate: selectedDate)
                            }
                        } label: {
                            Text(Strings.recreate).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                        }.background(Colors.mainColor).border(Colors.white, width: 1).cornerRadius(7.0).overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                        if routeDetails["type"] as! String != "P" {
                            Spacer()
                        }
                    }
                }
                    .padding(10)
            }
                .frame(width: UIScreen.main.bounds.width)
                .background(Colors.white)
            , alignment: .bottomLeading
        )
    }
}
