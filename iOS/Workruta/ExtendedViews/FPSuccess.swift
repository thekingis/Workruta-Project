//
//  FPSuccess.swift
//  Workruta
//
//  Created by The KING on 07/07/2022.
//

import SwiftUI

struct FPSuccess: View {
    
    let this: ForgotPassViewController
    let that: ForgotPassUIView
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 0) {
                ZStack{
                    Text(Strings.your_password_has_been_successfully_changed)
                        .foregroundColor(Colors.mainColor)
                        .font(.system(size: 16))
                        .multilineTextAlignment(.center)
                }
                .frame(maxHeight: .infinity)
                .padding(.horizontal, 20)
                HStack(alignment: .center) {
                    Spacer()
                    Button {
                        this.finish()
                    } label: {
                        Text(Strings.go_to_login)
                            .foregroundColor(Colors.white)
                            .padding(.vertical, 15)
                            .padding(.horizontal, 20)
                            .font(.system(size: 18))
                            .cornerRadius(7.0)
                    }
                    .background(Colors.mainColor)
                    .cornerRadius(7.0)
                    Spacer()
                }
                .padding(10)
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
    }
}
