//
//  NonScrollableView.swift
//  Workruta
//
//  Created by The KING on 07/06/2022.
//

import Foundation
import SwiftUI

struct NonScrollableView<Content>: View where Content: View {
    @Binding var canScroll: Bool
    var content: () -> Content
    
    var body: some View {
        if canScroll {
            ScrollView(.horizontal, showsIndicators: false, content: content)
        } else {
            content()
        }
    }
    
}
