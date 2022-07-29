//
//  PaymentsViewController.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import UIKit
import SwiftUI

class PaymentsViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: PaymentsUIView(this: self))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }

    }
    
    func listenToClicks(_ key: String, this: PaymentsUIView){
        let storyboard = UIStoryboard(name: "Extras", bundle: nil)
        switch key {
        case "idCard":
            let viewController  = storyboard.instantiateViewController(identifier: "IdentityView") as IdentityViewController
            viewController.paymentsUIView = this
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case "licenceDetail":
            let viewController  = storyboard.instantiateViewController(identifier: "LicenceView") as LicenceViewController
            viewController.paymentsUIView = this
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case "carDetail":
            let viewController  = storyboard.instantiateViewController(identifier: "CarView") as CarViewController
            viewController.paymentsUIView = this
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case "bankDetail":
            let viewController  = storyboard.instantiateViewController(identifier: "BankView") as BankViewController
            viewController.paymentsUIView = this
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        default:
            return
        }
    }

}
