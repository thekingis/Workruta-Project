//
//  CircularProgressView.swift
//  Workruta
//
//  Created by The KING on 14/06/2022.
//

import SwiftUI

struct CircularProgressView: View {
    let progress: Double
    
    var body: some View {
        ZStack {
            Circle().stroke(Colors.ash, lineWidth: 5)
            Circle().trim(from: 0, to: progress).stroke(Colors.green, style: StrokeStyle(lineWidth: 5, lineCap: .round)).rotationEffect(.degrees(-90)).animation(.easeOut, value: progress)
        }
    }
    
}
