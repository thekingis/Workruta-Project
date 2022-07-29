//
//  FPStart.swift
//  Workruta
//
//  Created by The KING on 07/07/2022.
//

import SwiftUI

struct FPStart: View {
    
    let this: ForgotPassViewController
    let that: ForgotPassUIView
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 20) {
                Text(Strings.a_verification_code_will_be_sent_to_you)
                    .foregroundColor(Colors.mainColor)
                    .font(.system(size: 16))
                    .multilineTextAlignment(.center)
                    .padding(bottom: 30)
                Button {
                    that.gotoFragment(index: 1)
                } label: {
                    Text(Strings.get_code_through_e_mail)
                        .frame(maxWidth: .infinity, alignment: .center)
                        .background(Colors.white)
                        .foregroundColor(Colors.mainColor)
                        .padding(.vertical, 10)
                        .font(.system(size: 16))
                        .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                }
                Button {
                    that.gotoFragment(index: 2)
                } label: {
                    Text(Strings.get_code_through_phone_number)
                        .frame(maxWidth: .infinity, alignment: .center)
                        .background(Colors.white)
                        .foregroundColor(Colors.mainColor)
                        .padding(.vertical, 10)
                        .font(.system(size: 16))
                        .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                }

            }
            .padding(.horizontal, 20)
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
    }
}
