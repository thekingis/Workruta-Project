//
//  ForgotPassViewController.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import UIKit
import SwiftUI

class ForgotPassViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: ForgotPassUIView(this: self))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }

    }

}
