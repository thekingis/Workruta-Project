//
//  CreateRouteViewController.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import UIKit
import SwiftUI
import GooglePlaces

class CreateRouteViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    private var that: CreateRouteUIView!
    private var myId: String!
    private var index = -1
    let autocompleteCtrller = GMSAutocompleteViewController()
    let filter = GMSAutocompleteFilter()
    let fields: GMSPlaceField = [.name, .coordinate]

    override func viewDidLoad() {
        super.viewDidLoad()
        myId = UserDefaults.standard.string(forKey: "myId")
        
        if controlView != nil {
            let childView = UIHostingController(rootView: CreateRouteUIView(this: self, myId: myId))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
            
            filter.type = .establishment
            filter.countries = [Constants.country]
            autocompleteCtrller.delegate = self
            autocompleteCtrller.autocompleteFilter = filter
            autocompleteCtrller.placeFields = fields
            autocompleteCtrller.primaryTextColor = UIColor.black
            autocompleteCtrller.secondaryTextColor = UIColor.lightGray
            autocompleteCtrller.tableCellSeparatorColor = UIColor.lightGray
            autocompleteCtrller.tableCellBackgroundColor = UIColor.white
            UITextField.appearance(whenContainedInInstancesOf: [UISearchBar.self]).defaultTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor.black]
        }

    }
    
    func openAutoSuggest(that: CreateRouteUIView, index: Int) {
        self.that = that
        self.index = index
        self.present(autocompleteCtrller, animated: true, completion: nil)
    }

}

extension CreateRouteViewController: GMSAutocompleteViewControllerDelegate {
    func viewController(_ viewController: GMSAutocompleteViewController, didAutocompleteWith place: GMSPlace) {
        let address = place.name!
        let latitude = place.coordinate.latitude
        let longitude = place.coordinate.longitude
        that.setLocationValues(address: address, latitude: latitude, longitude: longitude, index: index)
        dismiss(animated: true, completion: nil)
    }
    
    func viewController(_ viewController: GMSAutocompleteViewController, didFailAutocompleteWithError error: Error) {
    }
    
    func wasCancelled(_ viewController: GMSAutocompleteViewController) {
        dismiss(animated: true, completion: nil)
    }
    
    func didRequestAutocompletePredictions(_ viewController: GMSAutocompleteViewController) {
    }
    
    func didUpdateAutocompletePredictions(_ viewController: GMSAutocompleteViewController) {
    }
}
