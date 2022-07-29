//
//  ProfileDataUIView.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import SwiftUI

struct ProfileDataUIView: View {
    
    let this: ProfileViewController
    let headerKey: String
    let dataObject: [String: Any]
    let access: Bool
    let headers: [String: String] = [
        "data": "Profile Details",
        "licenceDetail": "Licence Details",
        "carDetail": "Car Details",
        "bankDetail": "Bank Details",
        "email": "Email",
        "phone": "Phone Number",
        "gender": "Gender",
        "address": "Residential Address",
        "licenceNo": "Licence Number",
        "licenceCat": "Licence Category",
        "classIndex": "Class",
        "stateIndex": "Issued State",
        "isDate": "Issued Date",
        "exDate": "Expiry Date",
        "plateNumber": "Plate Number",
        "carProduct": "Car Make",
        "carModel": "Car Model",
        "bank": "Bank",
        "accountNo": "Account Number"
    ]
    let keys: [String] = [
        "email",
        "phone",
        "gender",
        "address",
        "licenceNo",
        "licenceCat",
        "classIndex",
        "stateIndex",
        "isDate",
        "exDate",
        "plateNumber",
        "carProduct",
        "carModel",
        "bank",
        "accountNo"
    ]
    
    var body: some View {
        let header = headers[headerKey]
        let c = keys.count
        VStack {
            VStack {
                HStack {
                    Text(header!)
                        .foregroundColor(Colors.black)
                        .font(.system(size: 18, weight: .bold))
                    Spacer()
                    if access {
                        Button {
                            this.listenToClicks(headerKey)
                        } label: {
                            VStack {
                                HStack{
                                    Image(systemName: "pencil")
                                        .resizable()
                                        .frame(width: 20, height: 20)
                                        .foregroundColor(Colors.asher)
                                    Text(Strings.edit)
                                        .foregroundColor(Colors.asher)
                                        .font(.system(size: 17))
                                }
                                .padding(5)
                            }
                            .background(Colors.white)
                            .cornerRadius(7)
                        }

                    }
                }
                .padding(10)
            }
            .background(Colors.ash)
            ForEach(0..<c, id: \.self){ index in
                let key = keys[index]
                if (headers.containsKey(key) && dataObject.containsKey(key)) && dataObject[key] != nil && (key != "address" || access) {
                    let head = headers[key]!
                    if let t = (dataObject[key] as? String) {
                        let text = getText(key, t)
                        HStack {
                            Text(head)
                                .foregroundColor(Colors.black)
                                .font(.system(size: 18))
                            Spacer()
                        }
                        .padding(10)
                        VStack {
                            HStack {
                                Text(text)
                                    .foregroundColor(Colors.asher)
                                    .font(.system(size: 18))
                                Spacer()
                            }
                            .padding(vertical: 10, horizontal: 5)
                            .border(Colors.asher, width: 1.0)
                            .cornerRadius(7)
                        }
                        .padding(.horizontal, 10)
                    }
                }
            }
        }
        .padding(top: 30)
    }
    
    func getText(_ k: String, _ s: String) -> String {
        var text: String
        switch k {
        case "phone":
            text = Functions.formatPhoneNumber(phoneNumber: s)
        case "isDate", "exDate":
            text = Functions.convertDate(s)
        case "classIndex":
            let index = Int(s)
            text = Constants.classes[index!]
        case "stateIndex":
            let index = Int(s)
            text = Constants.allStates[index!]
        case "bank":
            let index = Int(s)
            text = Constants.allBanks[index!]
        default:
            text = s
        }
        return text
    }
    
}
