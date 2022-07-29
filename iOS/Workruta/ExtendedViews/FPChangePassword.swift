//
//  FPChangePassword.swift
//  Workruta
//
//  Created by The KING on 07/07/2022.
//

import SwiftUI

struct FPChangePassword: View {
    
    let this: ForgotPassViewController
    let that: ForgotPassUIView
    @State var newPass = ""
    @State var conPass = ""
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 0) {
                ZStack{
                    VStack(spacing: 20){
                        Text(Strings.create_new_password)
                            .foregroundColor(Colors.mainColor)
                            .font(.system(size: 16))
                            .multilineTextAlignment(.center)
                        SecureField("", text: $newPass){
                            UIApplication.shared.hideKeyboard(hide: false)
                        }
                        .modifier(PlaceholderStyle(show: newPass.isEmpty, text: Strings.new_password))
                        .padding(10)
                        .background(Colors.white).cornerRadius(10)
                        .foregroundColor(Colors.black)
                        .font(.system(size: 17))
                        .frame(maxWidth: .infinity)
                        .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                        .disabled(that.requesting)
                        SecureField("", text: $conPass){
                            UIApplication.shared.hideKeyboard(hide: false)
                        }
                        .modifier(PlaceholderStyle(show: conPass.isEmpty, text: Strings.confirm_password))
                        .padding(10)
                        .background(Colors.white).cornerRadius(10)
                        .foregroundColor(Colors.black)
                        .font(.system(size: 17))
                        .frame(maxWidth: .infinity)
                        .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                        .disabled(that.requesting)
                    }
                }
                .frame(maxHeight: .infinity)
                .padding(.horizontal, 20)
                HStack(alignment: .center) {
                    Spacer()
                    Button {
                        if !that.requesting {
                            that.changePassword(newPass: newPass, conPass: conPass)
                        }
                    } label: {
                        Text(Strings.change_password)
                            .foregroundColor(Colors.white)
                            .padding(.vertical, 15)
                            .padding(.horizontal, 20)
                            .font(.system(size: 18))
                            .cornerRadius(7.0)
                    }
                    .background(Colors.mainColor)
                    .cornerRadius(7.0)
                }
                .padding(10)
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
    }
}
