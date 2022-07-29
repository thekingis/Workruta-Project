//
//  LoginUIView.swift
//  Workruta
//
//  Created by The KING on 10/06/2022.
//

import SwiftUI

struct LoginUIView: View {
    
    let this: LoginViewController
    @ObservedObject var models: Models
    
    var body: some View {
        ZStack{
            ScrollView(showsIndicators: false){
                VStack(spacing: 30){
                    HStack(alignment: .center){
                        Spacer()
                        Image("icon").resizable().padding(EdgeInsets(top: 0, leading: 0, bottom: 10, trailing: 0)).frame(width: 150, height: 160, alignment: .center)
                        Text(Strings.Workruta).foregroundColor(Colors.mainColor).font(.system(size: 35, weight: .bold))
                        Spacer()
                    }.padding(top: 50)
                    Text(Strings.signIn).foregroundColor(Colors.mainColor).font(.system(size: 18, weight: .bold))
                    TextField("", text: $models.email){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: models.email.isEmpty, text: Strings.email)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(models.requesting)
                    SecureField("", text: $models.password){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: models.password.isEmpty, text: Strings.password)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(models.requesting)
                    Spacer().frame(height: 70)
                }
            }
        }.background(Colors.white)
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading).overlay(
                HStack{
                    (Text(Image(systemName: "chevron.left")) + Text(Strings.back)).foregroundColor(Colors.mainColor).padding(10).font(.system(size: 18)).onTapGesture {
                        if !models.requesting {
                            this.goBack()
                        }
                    }
                    Spacer()
                }.background(Colors.white)
            , alignment: .topLeading
        ).overlay(
            HStack(alignment: .center){
                Button {
                    if !models.requesting {
                        this.forgotPassWord()
                    }
                } label: {
                    Text(Strings.forgotPass).foregroundColor(Colors.mainColor).padding(10)
                }
                Spacer()
                Button {
                    UIApplication.shared.hideKeyboard(hide: true)
                    if !models.requesting {
                        this.signInUser()
                    }
                } label: {
                    if !models.requesting {
                        Text(Strings.signIn).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                    } else {
                        GIFView(gifName: "loader").frame(width: 30, height: 30, alignment: .center).padding(EdgeInsets(top: 10.7, leading: 37, bottom: 10.7, trailing: 37))
                    }
                }.background(!models.requesting ? Colors.mainColor : Colors.mainColorFade).border(Colors.white, width: 1).cornerRadius(10.0).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
            }.padding(left: 10, right: 10, bottom: 10).frame(width: UIScreen.main.bounds.width).background(Colors.white)
            , alignment: .bottom
        ).onTapGesture {
                UIApplication.shared.hideKeyboard(hide: true)
            }
    }
}
