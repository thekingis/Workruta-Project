//
//  EditProfileUIView.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import SwiftUI

struct EditProfileUIView: View {
    
    let this: EditProfileViewController
    let myId: String
    let genderLists = ["Female", "Male", "Others"]
    @State var fetching = true
    @State var requesting = false
    @State var iFName = ""
    @State var iLName = ""
    @State var iAddress = ""
    @State var iGender = ""
    @State var fName = ""
    @State var lName = ""
    @State var address = ""
    @State var gender = ""
    @State var password = ""
    @State var latitude = 0.0
    @State var longitude = 0.0
    @State var iLatitude = 0.0
    @State var iLongitude = 0.0
    
    var body: some View {
        ZStack {
            Colors.white
            ScrollView{
                VStack(spacing: 20) {
                    TextField("", text: $fName){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: fName.isEmpty, text: Strings.first_name)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(requesting)
                    TextField("", text: $lName){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: lName.isEmpty, text: Strings.last_name)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(requesting)
                    TextField("", text: $address){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: address.isEmpty, text: Strings.address)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(true).onTapGesture {
                        if !requesting {
                            this.openAutoSuggest()
                        }
                    }
                    VStack(alignment: .leading) {
                        Text(Strings.gender).foregroundColor(Colors.mainColor).font(.system(size: 17, weight: .bold)).padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 0))
                        RadioButtonGroup(items: genderLists, direction: "horizontal", selectedId: $gender) { selected in
                            gender = selected
                        }.padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 0))
                    }
                    SecureField("", text: $password){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: password.isEmpty, text: Strings.password)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(requesting)
                }
                .padding(top: 20, bottom: 30)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(top: 40)
            if fetching {
                ZStack{
                    Colors.whiteFade
                    GIFView(gifName: "loader")
                        .frame(width: 30, height: 30, alignment: .center)
                }
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .overlay(
            HStack(alignment: .center, spacing: 10){
                (Text(Image(systemName: "chevron.left")) + Text(Strings.personal_info)).foregroundColor(Colors.white).padding(10).font(.system(size: 18)).onTapGesture {
                    this.finish()
                }
                Spacer()
            }
                .background(Colors.mainColor)
            , alignment: .topLeading
        )
        .overlay(
            HStack(alignment: .center){
                Spacer()
                Button {
                    UIApplication.shared.hideKeyboard(hide: true)
                    if !requesting {
                        saveData()
                    }
                } label: {
                    if !requesting {
                        Text(Strings.save).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                    } else {
                        GIFView(gifName: "loader").frame(width: 30, height: 30, alignment: .center).padding(EdgeInsets(top: 10.7, leading: 37, bottom: 10.7, trailing: 37))
                    }
                }.background(!requesting ? Colors.mainColor : Colors.mainColorFade).border(Colors.white, width: 1).cornerRadius(10.0).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
            }.padding(left: 10, right: 10, bottom: 10).background(Colors.white)
            , alignment: .bottom
        ).onTapGesture {
            UIApplication.shared.hideKeyboard(hide: true)
        }.onAppear(){
            this.initialize(this: self)
            getData()
        }
    }
    
    func saveData() {
        if fName == "" || lName == "" || address == "" || gender == "" || password == "" {
            this.showAlertBox(title: "", msg: "Please fill in all fields", btnText: "Close")
            return
        }
        if iFName == fName && iLName == lName && iLatitude == latitude && iLongitude == longitude && iGender == gender {
            this.showAlertBox(title: "", msg: "No changes made", btnText: "Close")
            return
        }
        self.requesting = true
        guard let url = URL(string: Constants.saveInfoUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "fName": fName,
            "lName": lName,
            "address": address,
            "gender": gender,
            "latitude": String(latitude),
            "longitude": String(longitude),
            "password": password
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
                                self.iFName = self.fName
                                self.iLName = self.lName
                                self.iAddress = self.address
                                self.iGender = self.gender
                                self.iLatitude = self.latitude
                                self.iLongitude = self.longitude
                                self.this.showAlertBox(title: "", msg: "Data Saved", btnText: "Close")
                            } else {
                                let dataStr = object["dataStr"] as! String
                                self.this.showAlertBox(title: "", msg: dataStr, btnText: "Close")
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
    
    func getData() {
        guard let url = URL(string: Constants.editorUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "cat": "profile"
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
                            self.fName = object["fName"] as! String
                            self.lName = object["lName"] as! String
                            self.address = object["address"] as! String
                            self.gender = object["gender"] as! String
                            self.latitude = Double(object["latitude"] as! String)!
                            self.longitude = Double(object["longitude"] as! String)!
                            self.iFName = self.fName
                            self.iLName = self.lName
                            self.iAddress = self.address
                            self.iGender = self.gender
                            self.iLatitude = self.latitude
                            self.iLongitude = self.longitude
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
                self.fetching = false
            }
        }
        urlSession.resume()
    }
    
    func setAddressData(address: String, latitude: Double, longitude: Double){
        self.address = address
        self.latitude = latitude
        self.longitude = longitude
    }
    
}
