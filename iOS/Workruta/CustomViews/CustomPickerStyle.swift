//
//  CustomPicker.swift
//  Workruta
//
//  Created by The KING on 25/06/2022.
//

import SwiftUI

struct CustomPickerStyle: ViewModifier {
    
    @Binding var index: Int
    var items: [String]
    var font: SwiftUI.Font
    var padding: CGFloat
    
    func body(content: Content) -> some View {
        Menu {
            content
        } label: {
            HStack {
                if let labelText = items[index] {
                    Text(labelText)
                        .font(font)
                    Spacer()
                    Image(systemName: "triangle.fill")
                        .resizable()
                        .frame(width: 12, height: 8)
                        .rotationEffect(.degrees(180))
                }
            }
            .padding(padding)
            .frame(alignment: .leading)
            .background(Colors.white)
        }
    }
    
}
