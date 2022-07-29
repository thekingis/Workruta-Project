//
//  ProfileViewController.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import UIKit
import SwiftUI

class ProfileViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    public var myId: String!
    public var userId: String!
    private var access: Bool!

    override func viewDidLoad() {
        super.viewDidLoad()
        myId = UserDefaults.standard.string(forKey: "myId")!
        access = myId == userId
        
        if controlView != nil {
            let childView = UIHostingController(rootView: ProfileUIView(this: self, myId: myId, userId: userId, access: access))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }
    }
    
    func openMessenger(name: String, userEmail: String, photoUrl: URL) {
        let storyboard = UIStoryboard(name: "Extras", bundle: nil)
        let viewController  = storyboard.instantiateViewController(identifier: "MessageView") as MessageViewController
        viewController.userId = self.userId
        viewController.name = name
        viewController.photoUrl = photoUrl
        viewController.userEmail = userEmail.safeEmail()
        viewController.modalPresentationStyle = .fullScreen
        self.present(viewController, animated: true, completion: nil)
    }
    
    func listenToClicks(_ key: String){
        let storyboard = UIStoryboard(name: "Extras", bundle: nil)
        switch key {
        case "data":
            let viewController  = storyboard.instantiateViewController(identifier: "EditProfileView") as EditProfileViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case "licenceDetail":
            let viewController  = storyboard.instantiateViewController(identifier: "LicenceView") as LicenceViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case "carDetail":
            let viewController  = storyboard.instantiateViewController(identifier: "CarView") as CarViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case "bankDetail":
            let viewController  = storyboard.instantiateViewController(identifier: "BankView") as BankViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        default:
            return
        }
    }
    
    func changePhoto(){
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let controller = storyboard.instantiateViewController(identifier: "ChangePhotoView") as ChangePhotoViewController
        controller.isBackEnabled = true
        controller.modalPresentationStyle = .fullScreen
        self.present(controller, animated: true, completion: nil)
    }

}
