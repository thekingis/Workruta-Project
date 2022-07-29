//
//  MainViewController.swift
//  Workruta
//
//  Created by The KING on 04/06/2022.
//

import UIKit
import SwiftUI

class MainViewController: UIViewController {
    
    @IBOutlet weak var mainView : UIView!
    var controller: UIViewController!
        
    override func viewDidLoad() {
        super.viewDidLoad()
        let this = self
        
        //uiTextView.attributedText = fromKanaSoft.htmlToAttributedString
        let childView = UIHostingController(rootView: LaunchUIView())
        addChild(childView)
        childView.view.frame = mainView.bounds
        mainView.addSubview(childView.view)

        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        
        let loggedIn = UserDefaults.standard.bool(forKey: "loggedIn")
        if !loggedIn {
            controller = storyboard.instantiateViewController(identifier: "StartView") as StartViewController
            let phoneSet = UserDefaults.standard.bool(forKey: "phoneSet")
            if phoneSet {
                let phoneVerified = UserDefaults.standard.bool(forKey: "phoneVerified")
                let time = self.time()
                let expTime = UserDefaults.standard.integer(forKey: "expTime")
                if !phoneVerified {
                    if time > expTime {
                        Functions().removeAllUserCaches()
                    } else {
                        controller = storyboard.instantiateViewController(identifier: "SigninView") as SigninViewController
                    }
                } else {
                    controller = storyboard.instantiateViewController(identifier: "SignupView") as SignupViewController
                }
            }
        } else {
            let pictured = UserDefaults.standard.bool(forKey: "pictured")
            if !pictured {
                let controller1 = storyboard.instantiateViewController(identifier: "ChangePhotoView") as ChangePhotoViewController
                controller1.isBackEnabled = false
                DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                    this.dismiss(animated: false)
                    controller1.modalPresentationStyle = .fullScreen
                    controller1.modalTransitionStyle = .flipHorizontal
                    self.present(controller1, animated: true, completion: nil)
                }
            } else {
                controller = storyboard.instantiateViewController(identifier: "DashboardView") as DashboardViewController
            }
        }
        
        if controller != nil {
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                this.dismiss(animated: false)
                self.controller.modalPresentationStyle = .fullScreen
                self.controller.modalTransitionStyle = .flipHorizontal
                self.present(self.controller, animated: true, completion: nil)
            }
            
        }
        
    }

}
