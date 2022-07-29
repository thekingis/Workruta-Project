//
//  CarUIView.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import SwiftUI

struct CarUIView: View {
    
    let this: CarViewController
    let paymentsUIView: PaymentsUIView?
    let myId: String
    @State var fetching = true
    @State var requesting = false
    @State var manualInput = false
    @State var available = false
    @State var carIndex = 0
    @State var plateNumber = ""
    @State var carProduct = ""
    @State var carModel = ""
    @State var password = ""
    @State var iPlateNumber = ""
    @State var iCarProduct = ""
    @State var iCarModel = ""
    @State var iCarIndex = 0
    
    var body: some View {
        ZStack {
            Colors.white
            ScrollView{
                VStack(spacing: 20) {
                    TextField("", text: $plateNumber){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: plateNumber.isEmpty, text: Strings.plate_number)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(requesting)
                    if manualInput {
                        TextField("", text: $carProduct){
                            UIApplication.shared.hideKeyboard(hide: false)
                        }.modifier(PlaceholderStyle(show: carProduct.isEmpty, text: Strings.car_product)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(requesting)
                    } else {
                        Picker("", selection: $carIndex){
                            ForEach(0..<Constants.allCars.count, id: \.self){ index in
                                Text(Constants.allCars[index])
                            }
                        }
                        .customPickerStyle(index: $carIndex, items: Constants.allCars, font: .system(size: 17), padding: 10)
                        .frame(width: UIScreen.main.bounds.width - 50)
                        .overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
                        .disabled(requesting)
                    }
                    HStack {
                        Spacer()
                        Text(manualInput ? Strings.select_car_product : Strings.type_car_product)
                            .foregroundColor(Colors.mainColor)
                            .onTapGesture {
                                if !requesting {
                                    manualInput.toggle()
                                }
                            }
                    }
                    .frame(width: UIScreen.main.bounds.width - 50)
                    TextField("", text: $carModel){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: carModel.isEmpty, text: Strings.car_model)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(requesting)
                    SecureField("", text: $password){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: password.isEmpty, text: Strings.password)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(requesting)
                }
                .padding(top: 20, bottom: 80)
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
                (Text(Image(systemName: "chevron.left")) + Text(Strings.car_details)).foregroundColor(Colors.white).padding(10).font(.system(size: 18)).onTapGesture {
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
            }.padding(5).background(Colors.white)
            , alignment: .bottom
        ).onTapGesture {
            UIApplication.shared.hideKeyboard(hide: true)
        }.onAppear(){
            getData()
        }
    }
    
    func saveData() {
        if plateNumber == "" || (carProduct == "" && manualInput) || (carIndex == 0 && !manualInput) || carModel == "" || password == "" {
            this.showAlertBox(title: "", msg: "Please fill in all fields", btnText: "Close")
            return
        }
        if plateNumber == iPlateNumber && ((!manualInput && carIndex == iCarIndex) || (manualInput && carProduct == iCarProduct)) && carModel == iCarModel {
            this.showAlertBox(title: "", msg: "No changes made", btnText: "Close")
            return
        }
        self.requesting = true
        guard let url = URL(string: Constants.submitCarUrl) else {
            print("URL not found")
            return
        }
        let parameters = getParameters()
        let datas = parameters.toQueryString
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let task = URLSession.shared.dataTask(with: request) { data, _, error in
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
                                self.iPlateNumber = self.plateNumber
                                self.iCarModel = self.carModel
                                if manualInput {
                                    self.iCarProduct = self.carProduct
                                    self.carIndex = 0
                                    self.iCarIndex = self.carIndex
                                } else {
                                    self.iCarIndex = self.carIndex
                                    self.carProduct = ""
                                    self.iCarProduct = self.carProduct
                                }
                                if self.paymentsUIView != nil {
                                    self.paymentsUIView!.setData("carDetail", "attached")
                                }
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
        task.resume()
    }
    
    func getData() {
        guard let url = URL(string: Constants.editorUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "cat": "car"
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
                            self.available = object["available"] as! Bool
                            if available {
                                self.plateNumber = object["plateNumber"] as! String
                                self.carModel = object["carModel"] as! String
                                let carMake = object["carProduct"] as! String
                                if Constants.allCars.contains(carMake) {
                                    self.carIndex = Constants.allCars.firstIndex(of: carMake)!
                                    self.iCarIndex = self.carIndex
                                } else {
                                    self.manualInput = true
                                    self.carProduct = carMake
                                    self.iCarProduct = self.carProduct
                                }
                            }
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
    
    func getParameters() -> [String: String] {
        let parameter: [String: String] = [
            "user": myId,
            "password": password,
            "plateNumber": plateNumber,
            "carProduct": manualInput ? carProduct : Constants.allCars[carIndex],
            "carModel": carModel
        ]
        return parameter
    }
}
