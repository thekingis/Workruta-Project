//
//  SigninUIView.swift
//  Workruta
//
//  Created by The KING on 06/06/2022.
//

import SwiftUI
import Introspect

struct SigninUIView: View {
    @Namespace var leftStack
    @Namespace var rightStack
    let this: SigninViewController
    @ObservedObject var models: Models
    
    var body: some View {
        ZStack{
            Colors.mainColor
            VStack {
                Image("icon_white").padding(EdgeInsets(top: 0, leading: 0, bottom: 30, trailing: 0))
                ScrollViewReader { reader in
                    ScrollView(.horizontal, showsIndicators: false){
                        HStack(alignment: .top){
                            VStack{
                                Text(Strings.phoneNumber).foregroundColor(Colors.white)
                                HStack(spacing: 10) {
                                    HStack(spacing: 0){
                                        Image("usa").resizable().frame(width: 17, height: 17)
                                        Text(Strings._1).foregroundColor(Colors.black).font(.system(size: 17))
                                    }.padding(10).background(Colors.white).cornerRadius(10)
                                    ZStack(alignment: .trailing){
                                        TextField("", text: $models.phoneNumber){
                                            UIApplication.shared.hideKeyboard(hide: false)
                                        }.modifier(PlaceholderStyle(show: models.phoneNumber.isEmpty, text: Strings._508_712_3456)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 130).keyboardType(UIKeyboardType.numberPad).disabled(models.requesting)
                                        if models.requesting {
                                            GIFView(gifName: "loader").frame(width: 30, height: 30, alignment: .center).padding(10)
                                        }
                                    }.frame(width: UIScreen.main.bounds.width - 130)
                                }.frame(width: UIScreen.main.bounds.width)
                            }.frame(width: UIScreen.main.bounds.width).id(leftStack)
                            VStack{
                                Text(Strings.verificationCode).foregroundColor(Colors.white)
                                HStack() {
                                    Spacer()
                                    ZStack(alignment: .trailing){
                                        TextField("", text: $models.vCode){
                                            UIApplication.shared.hideKeyboard(hide: false)
                                        }.padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: 114).keyboardType(UIKeyboardType.numberPad).disabled(models.requesting)
                                        if models.requesting {
                                            GIFView(gifName: "loader").frame(width: 30, height: 30, alignment: .center).padding(10)
                                        }
                                    }.frame(width: 114)
                                    Spacer()
                                }.frame(width: UIScreen.main.bounds.width)
                                Text(Strings.phn_vry).frame(width: UIScreen.main.bounds.width).foregroundColor(Colors.white).font(.system(size: 16)).padding(EdgeInsets(top: 20, leading: 20, bottom: 0, trailing: 20)).multilineTextAlignment(.center
                                )
                                Text(models.vCodeCountDown).frame(width: UIScreen.main.bounds.width).foregroundColor(Colors.white).padding(EdgeInsets(top: 0, leading: 20, bottom: 20, trailing: 20)).font(.system(size: 16, weight: .bold))
                            }.frame(width: UIScreen.main.bounds.width).id(rightStack)
                        }.introspectScrollView { scrollView in
                            scrollView.isScrollEnabled = models.signinViewCanScroll
                            if models.signinViewCanScroll  {
                                withAnimation() {
                                    if models.signinViewToScroll {
                                        reader.scrollTo(rightStack)
                                    } else {
                                        reader.scrollTo(leftStack)
                                    }
                                    this.disableScroll()
                                }
                            }
                        }
                    }.frame(width: UIScreen.main.bounds.width)
                }.frame(width: UIScreen.main.bounds.width)
            }.background(Colors.mainColor).edgesIgnoringSafeArea(.all).padding(EdgeInsets(top: 0, leading: 30, bottom: 20, trailing: 30))
        }.overlay(
            HStack(alignment: .center){
                if models.signinViewToScroll {
                    Button {
                        if !models.requesting {
                            models.signinViewToScroll = false
                            models.signinViewCanScroll = true
                            models.vCode = Strings._00_00
                            this.stopCountDown()
                            Functions().removeAllUserCaches()
                        }
                    } label: {
                        Text(Strings.changePhoneNumber).foregroundColor(Colors.white).padding(10)
                    }
                }
                Spacer()
                Button {
                    UIApplication.shared.hideKeyboard(hide: true)
                    if !models.requesting {
                        if models.signinViewToScroll {
                            let parameters: [String: Any] = [
                                "type": "normal",
                                "phoneNumberText": models.phoneNumber,
                                "code": models.vCode
                            ]
                            this.submitCode(this: this, parameters: parameters)
                        } else {
                            let parameters: [String: Any] = ["phoneNumber": models.phoneNumber]
                            this.submitNumber(parameters: parameters)
                        }
                    }
                } label: {
                    Text(Strings.next).foregroundColor(Colors.mainColor).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                }.background(Colors.white).border(Colors.mainColor, width: 1).cornerRadius(10.0).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
            }.padding(EdgeInsets(top: 0, leading: 0, bottom: 10, trailing: 10)).frame(width: UIScreen.main.bounds.width)
            , alignment: .bottom
        ).overlay(
            (Text(Image(systemName: "chevron.left")) + Text(Strings.back)).foregroundColor(Colors.white).padding(EdgeInsets(top: 10, leading: 40, bottom: 10, trailing: 10)).font(.system(size: 18)).onTapGesture {
                if !models.requesting {
                    models.signinViewToScroll = false
                    models.signinViewCanScroll = true
                    models.vCode = Strings._00_00
                    this.stopCountDown()
                    Functions().removeAllUserCaches()
                    SigninViewController.closeView(this: this)
                }
            }
            , alignment: .topLeading
        ).onTapGesture {
            UIApplication.shared.hideKeyboard(hide: true)
        }
    }
    
}
