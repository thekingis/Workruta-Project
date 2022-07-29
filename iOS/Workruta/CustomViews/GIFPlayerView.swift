//
//  GIFPlayerView.swift
//  Workruta
//
//  Created by The KING on 10/06/2022.
//

import SwiftUI

class GIFPlayerView: UIView {
    private let imageView = UIImageView()

    convenience init(gifName: String) {
       self.init()
       let gif = UIImage.gif(asset: gifName)
       imageView.image = gif
       imageView.contentMode = .scaleAspectFit
       self.addSubview(imageView)
    }

    override init(frame: CGRect) {
       super.init(frame: frame)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        imageView.frame = bounds
    }
}
