//
//  FPCode.swift
//  Workruta
//
//  Created by The KING on 07/07/2022.
//

import SwiftUI

struct FPCode: View {
    
    let this: ForgotPassViewController
    let that: ForgotPassUIView
    let prefText: [String] = [Strings.code_to_email, Strings.code_to_phne]
    @State var code = ""
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 0) {
                ZStack{
                    VStack {
                        Text(self.prefText[that.prefIndex])
                            .foregroundColor(Colors.mainColor)
                            .font(.system(size: 16))
                            .multilineTextAlignment(.center)
                            .padding(bottom: 30)
                        TextField("", text: $code){
                            UIApplication.shared.hideKeyboard(hide: false)
                        }
                        .padding(10)
                        .background(Colors.white).cornerRadius(10)
                        .foregroundColor(Colors.black)
                        .font(.system(size: 17))
                        .frame(width: 114)
                        .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                        .keyboardType(UIKeyboardType.numberPad)
                        .disabled(that.requesting)
                    }
                }
                .frame(maxHeight: .infinity)
                .padding(.horizontal, 20)
                HStack(alignment: .center) {
                    Spacer()
                    Button {
                        if !that.requesting {
                            that.getVerificationCode(code: code)
                        }
                    } label: {
                        Text(Strings.submit_code)
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
