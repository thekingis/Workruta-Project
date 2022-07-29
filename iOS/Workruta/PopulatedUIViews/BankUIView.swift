//
//  BankUIView.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import SwiftUI

struct BankUIView: View {
    
    let this: BankViewController
    let paymentsUIView: PaymentsUIView?
    let myId: String
    @State var fetching = true
    @State var requesting = false
    @State var available = false
    @State var bankIndex = 0
    @State var accountNo = ""
    @State var iBankIndex = 0
    @State var iAccountNo = ""
    @State var password = ""
    
    var body: some View {
        ZStack {
            Colors.white
            ScrollView{
                VStack(spacing: 20) {
                    Picker("", selection: $bankIndex){
                        ForEach(0..<Constants.allBanks.count, id: \.self){ index in
                            Text(Constants.allBanks[index])
                        }
                    }
                    .customPickerStyle(index: $bankIndex, items: Constants.allBanks, font: .system(size: 17), padding: 10)
                    .frame(width: UIScreen.main.bounds.width - 50)
                    .overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
                    .disabled(requesting)
                    TextField("", text: $accountNo){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: accountNo.isEmpty, text: Strings.account_number)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(requesting)
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
                (Text(Image(systemName: "chevron.left")) + Text(Strings.bank_account_details)).foregroundColor(Colors.white).padding(10).font(.system(size: 18)).onTapGesture {
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
        if bankIndex == 0 || accountNo == "" || password == "" {
            this.showAlertBox(title: "", msg: "Please fill in all fields", btnText: "Close")
            return
        }
        if bankIndex == iBankIndex && accountNo == iAccountNo {
            this.showAlertBox(title: "", msg: "No changes made", btnText: "Close")
            return
        }
        self.requesting = true
        guard let url = URL(string: Constants.submitBankUrl) else {
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
                                self.iBankIndex = self.bankIndex
                                self.iAccountNo = self.accountNo
                                self.this.showAlertBox(title: "", msg: "Data Saved", btnText: "Close")
                                if self.paymentsUIView != nil {
                                    self.paymentsUIView!.setData("bankDetail", "attached")
                                }
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
            "cat": "bank"
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
                                self.bankIndex = Int(object["bankIndex"] as! String)!
                                self.accountNo = object["accountNo"] as! String
                                self.iBankIndex = self.bankIndex
                                self.iAccountNo = self.accountNo
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
            "bank": String(bankIndex),
            "accountNo": accountNo
        ]
        return parameter
    }
}
