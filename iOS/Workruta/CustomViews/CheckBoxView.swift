//
//  CheckBoxView.swift
//  Workruta
//
//  Created by The KING on 18/06/2022.
//

import SwiftUI

struct CheckBoxView: View {
    
    @Binding var checked: Bool
    
    var body: some View {
        Image(systemName: checked ? "checkmark.square.fill" : "square")
            .foregroundColor(checked ? Colors.mainColor : Colors.asher)
            .onTapGesture {
                self.checked.toggle()
            }
    }
}
