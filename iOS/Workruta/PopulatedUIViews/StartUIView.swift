//
//  StartUIView.swift
//  Workruta
//
//  Created by The KING on 05/06/2022.
//

import UIKit
import SwiftUI

struct StartUIView: View {
    
    let this: StartViewController
    @State var sliderImg = "slide_0"
    @State var slideNum = 0;
    @State var botm = true
    @State var fadeOut = false
    @State var scrollSide = CGPoint(x: 0, y: 0)
    let uiImage = UIImage(named: "slide_0")
    let imgTimer = Timer.publish(every: 6.0, on: .main, in: .common).autoconnect()
    let sVwTimer = Timer.publish(every: 3.0, on: .main, in: .common).autoconnect()
    
    var body: some View {
        ZStack {
            AnimatedScrollView($scrollSide, animationDuration: 3.0, showsScrollIndicator: false, axis: .horizontal, disableScroll: true) {
                HStack {
                    Image(sliderImg).resizable().scaledToFit().aspectRatio(contentMode: .fill).transition(.opacity.animation(.easeInOut(duration: 0.3)))
                        .onReceive(imgTimer) { _ in
                            slideNum += 1
                            if slideNum == 3 {
                                slideNum = 0
                            }
                            withAnimation {
                                sliderImg = "slide_" + String(slideNum)
                            }
                    }
                }.padding(EdgeInsets(top: 0, leading: 290, bottom: -40, trailing: 0))
            }.frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading).onReceive(sVwTimer) { _ in
                var scrollX = scrollSide.x
                if scrollX == 0 {
                    let imgH = uiImage!.getHeight
                    let imgW = uiImage!.getWidth
                    let scrnH = UIScreen.main.bounds.height
                    let scrnW = UIScreen.main.bounds.width
                    let ratioH = imgH / scrnH
                    let scrllEnd = imgW / ratioH
                    scrollX = scrllEnd - scrnW
                } else {
                    scrollX = 0
                }
                scrollSide = CGPoint(x: scrollX, y: 0)
            }
        }.overlay(
            VStack {
                Button {
                    this.openNextPage(i: 0)
                } label: {
                    Text("Sign Up").frame(maxWidth: .infinity).foregroundColor(Colors.mainColor).padding(EdgeInsets(top: 20, leading: 0, bottom: 20, trailing: 0)).font(.system(size: 16))
                }.background(Colors.white).border(Colors.mainColor, width: 1).cornerRadius(10.0).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
                Spacer()
                Button {
                    this.openNextPage(i: 1)
                } label: {
                    Text("Sign In").foregroundColor(Colors.white).padding(EdgeInsets(top: 20, leading: 50, bottom: 20, trailing: 50)).font(.system(size: 16))
                }.frame(alignment: .bottomLeading).background(Colors.mainColor).border(Colors.white, width: 1).cornerRadius(10.0).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.white, lineWidth: 1.0))
            }.frame(height: 200).padding(EdgeInsets(top: 0, leading: 20, bottom: 10, trailing: 20))
            , alignment: .bottom
        )
    }
    
}
