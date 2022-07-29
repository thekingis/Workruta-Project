//
//  InboxViewController.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import UIKit
import SwiftUI

class InboxViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    private var myId: String!
    private var safeEmail: String!

    override func viewDidLoad() {
        super.viewDidLoad()
        myId = UserDefaults.standard.string(forKey: "myId")
        safeEmail = UserDefaults.standard.string(forKey: "email")!.safeEmail()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: InboxUIView(this: self, myId: myId, safeEmail: safeEmail))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }

    }
    
    func openMessenger(userId: String, name: String, userEmail: String, photoUrl: URL) {
        let storyboard = UIStoryboard(name: "Extras", bundle: nil)
        let viewController  = storyboard.instantiateViewController(identifier: "MessageView") as MessageViewController
        viewController.userId = userId
        viewController.name = name
        viewController.photoUrl = photoUrl
        viewController.userEmail = userEmail.safeEmail()
        viewController.modalPresentationStyle = .fullScreen
        self.present(viewController, animated: true, completion: nil)
    }

}
