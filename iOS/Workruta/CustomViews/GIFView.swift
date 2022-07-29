//
//  GIFView.swift
//  Workruta
//
//  Created by The KING on 10/06/2022.
//

import SwiftUI

struct GIFView: UIViewRepresentable {
    var gifName: String

    func updateUIView(_ uiView: UIView, context: UIViewRepresentableContext<GIFView>) {

    }


    func makeUIView(context: Context) -> UIView {
        return GIFPlayerView(gifName: gifName)
    }
}
