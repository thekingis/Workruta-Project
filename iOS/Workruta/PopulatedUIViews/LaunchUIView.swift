//
//  LaunchUIView.swift
//  Workruta
//
//  Created by The KING on 05/06/2022.
//

import SwiftUI

struct LaunchUIView: View {
    var body: some View {
        VStack{
            Spacer() 
            HStack{
                Spacer()
                Image("icon").resizable().frame(width: 150.0, height: 150.0)
                Spacer()
            }
            Spacer()
            HStack{
                Spacer()
                Text("from").foregroundColor(Colors.mainColor)
                Text("KanaSoft").bold().foregroundColor(Colors.mainColor)
                Spacer()
            }.padding(.bottom, 15)
        }.background(Colors.white)
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
    }
}
