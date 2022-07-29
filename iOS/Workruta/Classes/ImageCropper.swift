//
//  ImageCropper.swift
//  Workruta
//
//  Created by The KING on 14/06/2022.
//

import Foundation
import SwiftUI
import Mantis

struct ImageCropper: UIViewControllerRepresentable {
    
    typealias Coordinator = ImageCropperCoordinator
    let this: ChangePhotoUIView
    @Binding var uiImage: UIImage?
    @Binding var toShow: Bool
    
    func makeCoordinator() -> ImageCropperCoordinator {
        return ImageCropperCoordinator(this: this, image: $uiImage, toShow: $toShow)
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        
    }
    
    func makeUIViewController(context: UIViewControllerRepresentableContext<ImageCropper>) -> Mantis.CropViewController {
        var config = Mantis.Config()
        config.presetFixedRatioType = .alwaysUsingOnePresetFixedRatio(ratio: 1.0)
        let Editor = Mantis.cropViewController(image: uiImage!, config: config)
        Editor.delegate = context.coordinator
        return Editor
    }
    
}

class ImageCropperCoordinator: NSObject, CropViewControllerDelegate {
    
    let this: ChangePhotoUIView
    @Binding var uiImage: UIImage?
    @Binding var toShow: Bool
    
    init(this: ChangePhotoUIView, image: Binding<UIImage?>, toShow: Binding<Bool>) {
        self.this = this
        _uiImage = image
        _toShow = toShow
    }
    
    func cropViewControllerDidCrop(_ cropViewController: CropViewController, cropped: UIImage, transformation: Transformation, cropInfo: CropInfo) {
        uiImage = cropped
        this.showCroppedImage(croppedImage: cropped)
        toShow = false
    }
    
    func cropViewControllerDidFailToCrop(_ cropViewController: CropViewController, original: UIImage) {
        
    }
    
    func cropViewControllerDidCancel(_ cropViewController: CropViewController, original: UIImage) {
        toShow = false
    }
    
    func cropViewControllerDidBeginResize(_ cropViewController: CropViewController) {
        
    }
    
    func cropViewControllerDidEndResize(_ cropViewController: CropViewController, original: UIImage, cropInfo: CropInfo) {
        
    }
    
}
