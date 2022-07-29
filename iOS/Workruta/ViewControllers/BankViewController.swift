//
//  BankViewController.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import UIKit
import SwiftUI

class BankViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    public var paymentsUIView: PaymentsUIView!
    private var myId: String!

    override func viewDidLoad() {
        super.viewDidLoad()
        myId = UserDefaults.standard.string(forKey: "myId")
        
        if controlView != nil {
            let childView = UIHostingController(rootView: BankUIView(this: self, paymentsUIView: paymentsUIView, myId: myId))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }

    }

}
