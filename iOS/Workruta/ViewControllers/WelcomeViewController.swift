//
//  WelcomeViewController.swift
//  Workruta
//
//  Created by The KING on 12/06/2022.
//

import UIKit
import SwiftUI

class WelcomeViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: WelcomeUIView(this: self))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }
    }
    
    func proceedForward() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let controller = storyboard.instantiateViewController(identifier: "DashboardView") as DashboardViewController
        controller.modalPresentationStyle = .fullScreen
        self.present(controller, animated: true, completion: nil)
    }

}
