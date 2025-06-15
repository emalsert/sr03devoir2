import { Cloudinary } from '@cloudinary/url-gen';
import { auto } from '@cloudinary/url-gen/actions/resize';
import { autoGravity } from '@cloudinary/url-gen/qualifiers/gravity';
import axios from 'axios';

const cloudName = process.env.REACT_APP_CLOUDINARY_CLOUD_NAME || 'dskh9cxw4';
const cld = new Cloudinary({ cloud: { cloudName } });

// Service pour l'intégration Cloudinary (upload et récupération d'images)

export function getCloudinaryImage(publicId, width = 500, height = 500) {
  return cld
    .image(publicId)
    .format('auto')
    .quality('auto')
    .resize(auto().gravity(autoGravity()).width(width).height(height));
}

export const uploadToCloudinary = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', 'sr03_avatar'); //nom de notre preset sur cloudinary

  const res = await axios.post(
    'https://api.cloudinary.com/v1_1/dskh9cxw4/image/upload',
    formData
  );
  return res.data.public_id; 
}; 