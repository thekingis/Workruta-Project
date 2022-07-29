//
//  ForgotPassUIView.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import SwiftUI

struct ForgotPassUIView: View {
    
    let this: ForgotPassViewController
    let emptyMsgs: [String: String] = [
        "email": "Please fill in your Email address",
        "phone": "Please fill in your Phone Number",
        "code": "Incomplete Verification Code"
    ]
    @State var index = 0
    @State var prefIndex = 0
    @State var showAction = false
    @State var showBackButton = true
    @State var requesting = false
    @State var userId = ""
    @State var type = ""
    @State var text = ""
    
    var body: some View {
        ZStack {
            Colors.white
            ScrollViewReader { reader in
                ScrollView(.horizontal, showsIndicators: false){
                    HStack(spacing: 0) {
                        FPStart(this: this, that: self).frame(width: UIScreen.main.bounds.width).id(0)
                        FPEmail(this: this, that: self).frame(width: UIScreen.main.bounds.width).id(1)
                        FPPhone(this: this, that: self).frame(width: UIScreen.main.bounds.width).id(2)
                        FPCode(this: this, that: self).frame(width: UIScreen.main.bounds.width).id(3)
                        FPChangePassword(this: this, that: self).frame(width: UIScreen.main.bounds.width).id(4)
                        FPSuccess(this: this, that: self).frame(width: UIScreen.main.bounds.width).id(5)
                    }
                    .introspectScrollView { scrollview in
                        scrollview.addGestureRecognizer(UIPanGestureRecognizer())
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(top: 40)
                .onChange(of: index) { _ in
                    type = index == 1 ? "email" : index == 2 ? "phone" : type
                    withAnimation {
                        reader.scrollTo(index)
                    }
                }
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .overlay(
            HStack {
                if showBackButton {
                    (Text(Image(systemName: "chevron.left")) + Text(Strings.discard))
                        .foregroundColor(Colors.white)
                        .font(.system(size: 18))
                        .padding(10)
                        .onTapGesture {
                            onBackPressed()
                    }
                    Spacer()
                }
            }
                .background(Colors.mainColor)
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
        .overlay(
            ZStack{
                if requesting {
                    Colors.whiteFade
                    GIFView(gifName: "loader")
                        .frame(width: 30, height: 30, alignment: .center)
                }
            }
            , alignment: .topLeading
        )
        .onTapGesture {
            UIApplication.shared.hideKeyboard(hide: true)
        }
    }
    
    func getVerificationCode(value: String) {
        if value.isEmpty {
            let errorTxt = emptyMsgs[type]!
            this.showAlertBox(title: "", msg: errorTxt, btnText: "Close")
            return
        }
        guard let url = URL(string: Constants.sendMailUrl) else {
            print("URL not found")
            return
        }
        self.requesting = true
        self.text = value
        let parameters: [String: String] = [
            "type": self.type,
            "value": value
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
                    self.this.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as? Bool
                            let dataStr: String = (object["dataStr"] as? String)!
                            if noError! {
                                self.userId = dataStr
                                self.index = 3
                            } else {
                                self.this.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                            }
                        }
                    } else {
                        self.this.showAlertBox(title: "", msg: "No Data received", btnText: "Close")
                    }
                } catch {
                    self.this.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                }
            }
        }
        urlSession.resume()
    }
    
    func getVerificationCode(code: String) {
        if code.isEmpty {
            let errorTxt = emptyMsgs["code"]!
            this.showAlertBox(title: "", msg: errorTxt, btnText: "Close")
            return
        }
        guard let url = URL(string: Constants.verifyCodeUrl) else {
            print("URL not found")
            return
        }
        self.requesting = true
        let parameters: [String: String] = [
            "type": self.type,
            "phoneNumberText": self.text,
            "code": code
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
                    self.this.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as? Bool
                            let dataStr: String = (object["dataStr"] as? String)!
                            if noError! {
                                self.index = 4
                            } else {
                                self.this.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                            }
                        }
                    } else {
                        self.this.showAlertBox(title: "", msg: "No Data received", btnText: "Close")
                    }
                } catch {
                    self.this.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                }
            }
        }
        urlSession.resume()
    }
    
    func changePassword(newPass: String, conPass: String) {
        if newPass.isEmpty || conPass.isEmpty {
            this.showAlertBox(title: "", msg: "Please fill in all fields", btnText: "Close")
            return
        }
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        self.requesting = true
        let parameters: [String: String] = [
            "action": "changePassword",
            "password": newPass,
            "conPass": conPass,
            "user": self.userId
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
                    self.this.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as? Bool
                            let dataStr: String = (object["dataStr"] as? String)!
                            if noError! {
                                self.index = 5
                                self.showBackButton = false
                            } else {
                                self.this.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                            }
                        }
                    } else {
                        self.this.showAlertBox(title: "", msg: "No Data received", btnText: "Close")
                    }
                } catch {
                    self.this.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                }
            }
        }
        urlSession.resume()
    }
    
    func onBackPressed(){
        if self.index == 3 || self.index == 4 {
            showAction = true
        } else {
            this.finish()
        }
    }
    
    func gotoFragment(index: Int){
        self.index = index
    }
    
}
