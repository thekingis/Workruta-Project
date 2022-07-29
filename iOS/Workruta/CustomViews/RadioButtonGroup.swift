//
//  RadioButtonGroup.swift
//  Workruta
//
//  Created by The KING on 10/06/2022.
//

import UIKit
import SwiftUI
import Foundation

struct ColorInvert: ViewModifier {

    @Environment(\.colorScheme) var colorScheme

    func body(content: Content) -> some View {
        Group {
            if colorScheme == .dark {
                content.colorInvert()
            } else {
                content
            }
        }
    }
}

struct RadioButton: View {

    @Environment(\.colorScheme) var colorScheme

    let id: String
    let callback: (String)->()
    let selectedID : String
    let size: CGFloat
    let color: Color
    let textSize: CGFloat

    init(
        _ id: String,
        callback: @escaping (String)->(),
        selectedID: String,
        size: CGFloat = 20,
        color: Color = Colors.mainColor,
        textSize: CGFloat = 14
        ) {
        self.id = id
        self.size = size
        self.color = color
        self.textSize = textSize
        self.selectedID = selectedID
        self.callback = callback
            
    }

    var body: some View {
        Button(action:{
            self.callback(self.id)
        }) {
            HStack(alignment: .center, spacing: 10) {
                Image(systemName: self.selectedID == self.id ? "largecircle.fill.circle" : "circle")
                    .renderingMode(.original)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: self.size, height: self.size).foregroundColor(self.selectedID == self.id ? Colors.mainColor : Colors.black)
                Text(id)
                    .font(Font.system(size: textSize)).foregroundColor(self.selectedID == self.id ? Colors.mainColor : Colors.black)
                Spacer()
            }.foregroundColor(self.color)
        }
        .foregroundColor(self.color)
    }
}

struct RadioButtonGroup: View {

    let items : [String]
    let direction: String
    @Binding var selectedId: String
    
    let callback: (String) -> ()
    var body: some View {
        if direction == "vertical" {
            VStack {
                ForEach(0..<items.count) { index in
                    RadioButton(self.items[index], callback: self.radioGroupCallback, selectedID: self.selectedId)
                }
            }
        } else {
            HStack (spacing: 10){
                ForEach(0..<items.count) { index in
                    RadioButton(self.items[index], callback: self.radioGroupCallback, selectedID: self.selectedId)
                }
            }
        }
    }

    func radioGroupCallback(id: String) {
        selectedId = id
        callback(id)
    }
}
