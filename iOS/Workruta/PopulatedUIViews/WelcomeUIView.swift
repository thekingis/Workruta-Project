//
//  WelcomeUIView.swift
//  Workruta
//
//  Created by The KING on 12/06/2022.
//

import SwiftUI

struct WelcomeUIView: View {
    
    let this: WelcomeViewController
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 30){
                ZStack{
                    VStack(){
                    }.frame(width: 200, height: 200, alignment: .center).cornerRadius(150).overlay(RoundedRectangle(cornerRadius: 150.0).stroke(Colors.mainColor, lineWidth: 3.0)).background(Colors.mainColor).contentShape(Circle())
                    Image(systemName: "checkmark").resizable().foregroundColor(Colors.white).frame(width: 100, height: 100)
                }.frame(width: 200, height: 200, alignment: .center).cornerRadius(150).overlay(RoundedRectangle(cornerRadius: 150.0).stroke(Colors.mainColor, lineWidth: 3.0)).contentShape(Circle())
                Text(Strings.ss).multilineTextAlignment(.center).foregroundColor(Colors.green).font(.system(size: 17, weight: .bold))
            }
        }.background(Colors.white)
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading).overlay(
                HStack(alignment: .center){
                    Spacer()
                    Button {
                        this.proceedForward()
                    } label: {
                        Text(Strings._continue).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                    }.background(Colors.mainColor).border(Colors.white, width: 1).cornerRadius(10.0).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.white, lineWidth: 1.0))
                    Spacer()
                }.padding(EdgeInsets(top: 0, leading: 0, bottom: 10, trailing: 0))
                , alignment: .bottom
            )
    }
}
