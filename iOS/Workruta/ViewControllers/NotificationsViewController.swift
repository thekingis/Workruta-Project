//
//  NotificationsViewController.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import UIKit
import SwiftUI
import FirebaseDatabase

class NotificationsViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    private var database: DatabaseReference!
    private var myId: String!

    override func viewDidLoad() {
        super.viewDidLoad()
        database = Database.database().reference()
        myId = UserDefaults.standard.string(forKey: "myId")!
        
        if controlView != nil {
            let childView = UIHostingController(rootView: NotificationsUIView(this: self))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }

    }
    
    func openViewController(key: String, dataType: String, dataId: String, extraId: String) {
        let notificationsDB = database.child("notifications/\(myId!)/\(key)/unseen")
        notificationsDB.setValue(0)
        let firstSet: [String] = [
            "mergedRoute",
            "editRoute",
            "accepted",
            "startRoute",
            "endRoute"
        ]
        let secondSet: [String] = [
            "cancelRoute"
        ]
        let thirdSet: [String] = [
            "rating",
            "payment",
            "rejected",
            "requestForTmrw",
            "requestForToday",
            "mergedRouteForTmrw",
            "mergedRouteForToday",
            "unmergedRouteForTmrw",
            "unmergedRouteForToday"
        ]
        if firstSet.contains(dataType) {
            let storyboard = UIStoryboard(name: "Extras", bundle: nil)
            let viewController  = storyboard.instantiateViewController(identifier: "RouteView") as RouteViewController
            viewController.routeIdTo = extraId
            viewController.routeIdFrom = dataId
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
            return
        }
        if secondSet.contains(dataType) {
            let storyboard = UIStoryboard(name: "Extras", bundle: nil)
            let viewController  = storyboard.instantiateViewController(identifier: "RoutesView") as RoutesViewController
            viewController.routeId = extraId
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
            return
        }
        if thirdSet.contains(dataType) {
            let storyboard = UIStoryboard(name: "Extras", bundle: nil)
            let viewController  = storyboard.instantiateViewController(identifier: "RoutesView") as RoutesViewController
            viewController.routeId = dataId
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
            return
        }
    }

}
