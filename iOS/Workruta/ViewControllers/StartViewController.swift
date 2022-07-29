//
//  StartViewController.swift
//  Workruta
//
//  Created by The KING on 04/06/2022.
//

import UIKit
import SwiftUI

class StartViewController: UIViewController {

    @IBOutlet var controlView: UIView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: StartUIView(this: self))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }
        
    }
    
    func openNextPage(i: Int){
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        var uiController: UIViewController
        if i == 0 {
            uiController = storyboard.instantiateViewController(identifier: "SigninView") as SigninViewController
        } else {
            uiController = storyboard.instantiateViewController(identifier: "LoginView") as LoginViewController
        }
        uiController.modalPresentationStyle = .fullScreen
        self.present(uiController, animated: true, completion: nil)
    }

}
