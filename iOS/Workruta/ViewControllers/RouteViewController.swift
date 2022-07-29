//
//  RouteViewController.swift
//  Workruta
//
//  Created by The KING on 19/06/2022.
//

import UIKit
import SwiftUI

class RouteViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    private var myId: String!
    public var routeIdTo: String!
    public var routeIdFrom: String!
    public var this: PreviousRoutesViewController!
    public var routeIndex: Int!

    override func viewDidLoad() {
        super.viewDidLoad()
        myId = UserDefaults.standard.string(forKey: "myId")!
        
        if controlView != nil {
            let childView = UIHostingController(rootView: RouteUIView(this: self, that: this, routeIdTo: routeIdTo, routeIdFrom: routeIdFrom, myId: myId, routeIndex: routeIndex))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }
    }
    
    func openRoute(){
        let storyboard = UIStoryboard(name: "Extras", bundle: nil)
        let viewController  = storyboard.instantiateViewController(identifier: "RoutesView") as RoutesViewController
        viewController.routeId = routeIdFrom
        viewController.modalPresentationStyle = .fullScreen
        self.present(viewController, animated: true, completion: nil)
    }
    
    func openPage(index: Int, userId: String, name: String, userEmail: String, imageUrl: URL){
        switch index {
        case 0:
            let storyboard = UIStoryboard(name: "Extras", bundle: nil)
            let viewController  = storyboard.instantiateViewController(identifier: "MessageView") as MessageViewController
            viewController.userId = userId
            viewController.name = name
            viewController.photoUrl = imageUrl
            viewController.userEmail = userEmail.safeEmail()
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 1:
            let storyboard = UIStoryboard(name: "Dashboards", bundle: nil)
            let viewController  = storyboard.instantiateViewController(identifier: "ProfileView") as ProfileViewController
            viewController.userId = userId
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        default:
            return
        }
    }

}
