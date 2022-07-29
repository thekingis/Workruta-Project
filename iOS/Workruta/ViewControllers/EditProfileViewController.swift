//
//  EditProfileViewController.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import UIKit
import SwiftUI
import GooglePlaces

class EditProfileViewController: UIViewController {
    
    @IBOutlet weak var controlView : UIView!
    private var myId: String!
    private var this: EditProfileUIView!
    let autocompleteCtrller = GMSAutocompleteViewController()
    let filter = GMSAutocompleteFilter()
    let fields: GMSPlaceField = [.name, .coordinate]

    override func viewDidLoad() {
        super.viewDidLoad()
        myId = UserDefaults.standard.string(forKey: "myId")
        
        if controlView != nil {
            let childView = UIHostingController(rootView: EditProfileUIView(this: self, myId: myId))
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
    
    func initialize(this: EditProfileUIView){
        self.this = this
    }
    
    func openAutoSuggest() {
        self.present(autocompleteCtrller, animated: true, completion: nil)
    }

}

extension EditProfileViewController: GMSAutocompleteViewControllerDelegate {
    func viewController(_ viewController: GMSAutocompleteViewController, didAutocompleteWith place: GMSPlace) {
        this.setAddressData(address: place.name!, latitude: place.coordinate.latitude, longitude: place.coordinate.longitude)
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
