//
//  MessageViewController.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import UIKit
import SwiftUI

class MessageViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    public var userId: String!
    public var name: String!
    public var userEmail: String!
    public var photoUrl: URL!
    private var myId: String!
    private var access: Bool!

    override func viewDidLoad() {
        super.viewDidLoad()
        myId = UserDefaults.standard.string(forKey: "myId")
        access = myId == userId
        
        if controlView != nil {
            let childView = UIHostingController(rootView: MessageUIView(this: self, userEmail: userEmail, myId: myId, userId: userId, name: name, imageUrl: photoUrl, access: access))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }
    }
    
    func visitProfile(){
        let storyboard = UIStoryboard(name: "Dashboards", bundle: nil)
        let viewController  = storyboard.instantiateViewController(identifier: "ProfileView") as ProfileViewController
        viewController.userId = self.userId
        viewController.modalPresentationStyle = .fullScreen
        self.present(viewController, animated: true, completion: nil)
    }

}
