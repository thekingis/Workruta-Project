//
//  IdentityViewController.swift
//  Workruta
//
//  Created by The KING on 27/07/2022.
//

import UIKit
import SwiftUI

class IdentityViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    public var paymentsUIView: PaymentsUIView!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: IdentityUIView(this: self, paymentsUIView: paymentsUIView))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }

    }

}
