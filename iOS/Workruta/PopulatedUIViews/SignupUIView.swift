//
//  SignupUIView.swift
//  Workruta
//
//  Created by The KING on 10/06/2022.
//

import SwiftUI

struct SignupUIView: View {
    
    let this: SignupViewController
    @ObservedObject var models: Models
    @State var phoneNumber = Functions.formatPhoneNumber(phoneNumber: UserDefaults.standard.string(forKey: "phoneNumber")!)
    let genderLists = ["Female", "Male", "Others"]
    
    var body: some View {
        ZStack{
            ScrollView(.vertical, showsIndicators: false){
                Image("icon").resizable().padding(EdgeInsets(top: 0, leading: 0, bottom: 10, trailing: 0)).frame(width: 150, height: 160, alignment: .center)
                VStack(spacing: 30){
                    TextField("", text: $models.fName){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: models.fName.isEmpty, text: Strings.first_name)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(models.requesting)
                    TextField("", text: $models.lName){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: models.lName.isEmpty, text: Strings.last_name)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(models.requesting)
                    TextField("", text: $models.email){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: models.email.isEmpty, text: Strings.email)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(models.requesting)
                    TextField("", text: $phoneNumber){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(true)
                    SecureField("", text: $models.password){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: models.password.isEmpty, text: Strings.password)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(models.requesting)
                    SecureField("", text: $models.conPass){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: models.conPass.isEmpty, text: Strings.confirm_password)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(models.requesting)
                    VStack(alignment: .leading) {
                        Text(Strings.gender).foregroundColor(Colors.mainColor).font(.system(size: 17, weight: .bold)).padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 0))
                        RadioButtonGroup(items: genderLists, direction: "horizontal", selectedId: $models.gender) { selected in
                            models.gender = selected
                        }.padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 0))
                    }
                    TextField("", text: $models.address){
                        UIApplication.shared.hideKeyboard(hide: false)
                    }.modifier(PlaceholderStyle(show: models.address.isEmpty, text: Strings.address)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(true).onTapGesture {
                        if !models.requesting {
                            this.openAutoSuggest()
                        }
                    }
                    VStack(){
                    }.frame(height: 60)
                }
            }.frame(width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height)
        }.background(Colors.white)
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading).overlay(
            HStack(alignment: .center){
                Button {
                    if !models.requesting {
                        Functions().removeAllUserCaches()
                        this.changeNumber()
                    }
                } label: {
                    Text(Strings.changePhoneNumber).foregroundColor(Colors.mainColor).padding(10)
                }
                Spacer()
                Button {
                    UIApplication.shared.hideKeyboard(hide: true)
                    if !models.requesting {
                        this.signUpUser()
                    }
                } label: {
                    if !models.requesting {
                        Text(Strings.signUp).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                    } else {
                        GIFView(gifName: "loader").frame(width: 30, height: 30, alignment: .center).padding(EdgeInsets(top: 10.7, leading: 41.7, bottom: 10.7, trailing: 41.7))
                    }
                }.background(!models.requesting ? Colors.mainColor : Colors.mainColorFade).border(Colors.white, width: 1).cornerRadius(10.0).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
            }.padding(left: 10, right: 10, bottom: 10).frame(width: UIScreen.main.bounds.width).background(Colors.white)
            , alignment: .bottom
        ).onTapGesture {
            UIApplication.shared.hideKeyboard(hide: true)
        }
    }
}
