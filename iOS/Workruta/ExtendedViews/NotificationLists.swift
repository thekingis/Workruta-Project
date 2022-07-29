//
//  NotificationLists.swift
//  Workruta
//
//  Created by The KING on 24/07/2022.
//

import SwiftUI

struct NotificationLists: View {
    
    let this: NotificationsViewController
    let that: NotificationsUIView
    let notes: [String: Any]
    @State var accept = "Accept"
    @State var reject = "Reject"
    @State var showForA = false
    @State var showForR = false
    
    var body: some View {
        
        let key = notes["key"] as! String
        let dataId = notes["dataId"] as! String
        let dataType = notes["dataType"] as! String
        let dateStr = notes["date"] as! String
        let extraId = notes["extraId"] as! String
        let isSeen = notes["isSeen"] as! Bool
        let userFrom = notes["userFrom"] as! String
        let date = dateStr.convertToDate()
        let dateSTR = date.minify()
        let gifName = getGifName(dataType: dataType)
        let text = getText(dataType: dataType, extraId: extraId, userFrom: userFrom)
        let notToClick = dataType == "followRoute"
        
        if notToClick {
            VStack {
                HStack(alignment: .center, spacing: 10) {
                    GIFView(gifName: gifName)
                        .frame(width: 35, height: 35)
                    VStack(alignment: .center, spacing: 5){
                        Text(text)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .foregroundColor(Colors.black)
                            .multilineTextAlignment(.leading)
                            .font(.system(size: 15))
                            .lineLimit(2)
                        if !isSeen {
                            HStack (spacing: 15){
                                Spacer()
                                Button {
                                    if !showForA && !showForR {
                                        showForA = true
                                        that.sendAction(action: "accepted", userTo: userFrom, key: key, dataId: dataId, extraId: extraId)
                                    }
                                } label: {
                                    ZStack {
                                        if !showForA {
                                            Text(accept)
                                                .foregroundColor(Colors.white)
                                        } else {
                                            GIFView(gifName: "cupertino")
                                                .frame(width: 25, height: 25)
                                        }
                                    }
                                    .frame(width: 70, height: 45, alignment: .center)
                                    .background(Colors.green)
                                    .cornerRadius(5)
                                }
                                Button {
                                    if !showForA && !showForR {
                                        showForR = true
                                        that.sendAction(action: "rejected", userTo: userFrom, key: key, dataId: dataId, extraId: extraId)
                                    }
                                } label: {
                                    ZStack {
                                        if !showForR {
                                            Text(reject)
                                                .foregroundColor(Colors.white)
                                        } else {
                                            GIFView(gifName: "cupertino")
                                                .frame(width: 25, height: 25)
                                        }
                                    }
                                    .frame(width: 70, height: 45, alignment: .center)
                                    .background(Colors.normalRed)
                                    .cornerRadius(5)
                                }
                            }
                        }
                        Text(dateSTR)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                            .foregroundColor(Colors.asher)
                            .font(.system(size: 12))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(10)
            }
            .frame(maxWidth: .infinity)
            .background(isSeen ? Colors.white : Colors.mainColorLight)
            .border(Colors.asher, width: 1.0)
            .cornerRadius(7)
            .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.ash, lineWidth: 1.0))
        } else {
            Button {
                this.openViewController(key: key, dataType: dataType, dataId: dataId, extraId: extraId)
            } label: {
                VStack {
                    HStack(alignment: .center, spacing: 10) {
                        GIFView(gifName: gifName)
                            .frame(width: 35, height: 35)
                        VStack(alignment: .center, spacing: 5){
                            Text(text)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .foregroundColor(Colors.black)
                                .multilineTextAlignment(.leading)
                                .font(.system(size: 15))
                                .lineLimit(2)
                            Text(dateSTR)
                                .frame(maxWidth: .infinity, alignment: .trailing)
                                .foregroundColor(Colors.asher)
                                .font(.system(size: 12))
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(10)
                }
                .frame(maxWidth: .infinity)
                .background(isSeen ? Colors.white : Colors.mainColorLight)
                .border(Colors.asher, width: 1.0)
                .cornerRadius(7)
                .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.ash, lineWidth: 1.0))
            }
        }
    }
    
    func getGifName(dataType: String) -> String {
        var gifName: String
        switch (dataType){
            case "followRoute":
                gifName = "follow"
            case "mergedRoute":
                gifName = "match"
            case "editRoute":
                gifName = "edit_route"
            case "cancelRoute":
                gifName = "cancel_route"
            case "rejected":
                gifName = "rejected"
            case "accepted":
                gifName = "accepted"
            case "mergedRouteForTmrw":
                gifName = "merged"
            case "mergedRouteForToday":
                gifName = "merged"
            case "unmergedRouteForTmrw":
                gifName = "unmerged_tmrw"
            case "unmergedRouteForToday":
                gifName = "unmerged"
            case "requestForTmrw":
                gifName = "req_for_tmrw"
            case "requestForToday":
                gifName = "req_for_today"
            case "startRoute":
                gifName = "start_route"
            case "endRoute":
                gifName = "end_route"
            case "rating":
                gifName = "rating_star"
            case "payment":
                gifName = "paid"
            default:
                gifName = ""
        }
        return gifName
    }
    
    func getText(dataType: String, extraId: String, userFrom: String) -> String {
        var text: String
        switch (dataType){
            case "followRoute":
                text = "You have a request from a user to follow your ride"
            case "mergedRoute":
                text = "We have a match for your route"
            case "editRoute":
                text = "A route You are linked with have been edited. Check if it's okay with You"
            case "cancelRoute":
                text = "A route You are linked with have been cancelled. You can search for other available routes"
            case "rejected":
                text = "Your request to follow a ride has been declined. You can search for other available routes"
            case "accepted":
                text = "Your request to follow a ride has been approved"
            case "mergedRouteForTmrw":
                text = "<b><span style=\"color:#CC0000;\">Reminder:</span></b> You have a route for tomorrow"
            case "mergedRouteForToday":
                text = "<b><span style=\"color:#CC0000;\">Reminder:</span></b> You have a route for today"
            case "unmergedRouteForTmrw":
                text = "<b><span style=\"color:#CC0000;\">You have an unmerged route tomorrow. Please try to search for matching routes</span></b>"
            case "unmergedRouteForToday":
                text = "<b><span style=\"color:#CC0000;\">You have an unmerged route today. Please try to search for matching routes</span></b>"
            case "requestForTmrw":
                text = "<b>You have an unattended ride request for your route scheduled for tomorrow</b>"
            case "requestForToday":
                text = "<b>You have an unattended ride request for your route scheduled for today</b>"
            case "startRoute":
                text = "A ride you are linked to has been marked as <b><span style=\"color:#00CC00;\">Started</span></b>"
            case "endRoute":
                text = "A ride you are linked to has been marked as <b><span style=\"color:#CC0000;\">Ended</span></b>"
            case "rating":
                text = "<b><span style=\"color:#00CC00;\">Kudos:</span></b> You got a \(extraId) rating from a ride user"
            case "payment":
                let amount = extraId.stripeToDollar()
                text = "<b><span style=\"color:#00CC00;\">You have received \(amount) from \(userFrom)</span></b>"
            default:
                text = ""
        }
        return text.htmlToString
    }
    
}
