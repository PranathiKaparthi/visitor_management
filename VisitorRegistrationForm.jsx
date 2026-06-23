import React, { useState, useRef, useEffect } from 'react';

const VisitorRegistrationForm = ({ onSubmitSuccess }) => {
  // Form field states
  const [formData, setFormData] = useState({
    name: '',
    purpose: '',
    hostEmail: '',
    photo: '', // Captured Base64 Photo Data URL
  });

  // State for tracking validation errors
  const [errors, setErrors] = useState({});
  // State for form submission status
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  // Webcam States
  const [showWebcam, setShowWebcam] = useState(false);
  const [cameraStream, setCameraStream] = useState(null);
  const [videoDevices, setVideoDevices] = useState([]);
  const [selectedDeviceId, setSelectedDeviceId] = useState('');
  const [cameraError, setCameraError] = useState('');
  const [capturedImage, setCapturedImage] = useState(null);
  const [isCameraLoading, setIsCameraLoading] = useState(false);

  const videoRef = useRef(null);

  // Common purposes dropdown list
  const purposeOptions = [
    { value: '', label: 'Select a purpose' },
    { value: 'Official Meeting', label: 'Official Meeting' },
    { value: 'Interview', label: 'Interview' },
    { value: 'Vendor Visit', label: 'Vendor Visit' },
    { value: 'Delivery', label: 'Delivery' },
    { value: 'Maintenance', label: 'Maintenance' },
    { value: 'Guest Visit', label: 'Personal/Guest Visit' },
  ];

  // Email validation regex (standard compliant)
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

  // Handle generic input change
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    
    // Clear error for field while typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: null,
      }));
    }
  };

  // Perform client-side validation
  const validateForm = () => {
    const newErrors = {};

    // Name field validation
    if (!formData.name.trim()) {
      newErrors.name = 'Full name is required';
    } else if (formData.name.trim().length < 2) {
      newErrors.name = 'Full name must be at least 2 characters';
    }

    // Purpose field validation
    if (!formData.purpose) {
      newErrors.purpose = 'Please select the purpose of your visit';
    }

    // Host Email field validation
    if (!formData.hostEmail.trim()) {
      newErrors.hostEmail = 'Host email is required';
    } else if (!emailRegex.test(formData.hostEmail.trim())) {
      newErrors.hostEmail = 'Please enter a valid host email address';
    }

    // Photo field warning (not hard blocking, but recommended)
    if (!formData.photo) {
      newErrors.photo = 'Adding a visitor photo snapshot is highly recommended';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0 || (Object.keys(newErrors).length === 1 && newErrors.photo);
  };

  // WebCam Integration - Start Camera
  const startCamera = async (deviceId = '') => {
    setIsCameraLoading(true);
    setCameraError('');
    setCapturedImage(null);

    // Stop current stream if alive
    stopCamera();

    try {
      const constraints = {
        video: deviceId 
          ? { deviceId: { exact: deviceId } } 
          : { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' }
      };

      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      setCameraStream(stream);

      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }

      // Enumerate system's active video devices (cameras)
      const devices = await navigator.mediaDevices.enumerateDevices();
      const videoInputs = devices.filter((device) => device.kind === 'videoinput');
      setVideoDevices(videoInputs);
      
      if (videoInputs.length > 0 && !selectedDeviceId) {
        setSelectedDeviceId(videoInputs[0].deviceId);
      }
    } catch (err) {
      console.error('Camera capture service failure:', err);
      if (err.name === 'NotAllowedError') {
        setCameraError('Permission denied. Please grant webcam access in your browser settings.');
      } else if (err.name === 'NotFoundError' || err.name === 'DevicesNotFoundError') {
        setCameraError('No webcam or camera device could be detected on your system.');
      } else {
        setCameraError('Could not connect to camera: ' + err.message);
      }
    } finally {
      setIsCameraLoading(false);
    }
  };

  // Stop current active stream tracks
  const stopCamera = () => {
    if (cameraStream) {
      cameraStream.getTracks().forEach((track) => track.stop());
      setCameraStream(null);
    }
    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }
  };

  // Switch camera source on selection update
  const handleDeviceChange = (e) => {
    const deviceId = e.target.value;
    setSelectedDeviceId(deviceId);
    startCamera(deviceId);
  };

  // Capture Screenshot Snapshot Frame
  const captureSnapshot = () => {
    if (!videoRef.current) return;

    const canvas = document.createElement('canvas');
    const video = videoRef.current;
    
    // Match matching high resolution stream dimensions
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;

    const ctx = canvas.getContext('2d');
    
    // Draw mirrored to match screen representation if using front/user camera
    ctx.translate(canvas.width, 0);
    ctx.scale(-1, 1);
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

    const base64DataUrl = canvas.toDataURL('image/jpeg', 0.85);
    setCapturedImage(base64DataUrl);
    stopCamera();
  };

  // Accept Captured Snapshot
  const acceptPhoto = () => {
    if (capturedImage) {
      setFormData((prev) => ({
        ...prev,
        photo: capturedImage,
      }));
      // Clear photo error if any
      if (errors.photo) {
        setErrors((prev) => ({ ...prev, photo: null }));
      }
    }
    setShowWebcam(false);
  };

  // Discard captured snapshot or restart live feed
  const retakePhoto = () => {
    setCapturedImage(null);
    startCamera(selectedDeviceId);
  };

  // Cleanup effect
  useEffect(() => {
    return () => {
      stopCamera();
    };
  }, [cameraStream]);

  // Handle Form Submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitError(null);
    setSubmitSuccess(false);

    // Run dynamic validation check
    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      // Simulate API integration delay
      await new Promise((resolve) => setTimeout(resolve, 1500));

      setSubmitSuccess(true);
      if (onSubmitSuccess) {
        onSubmitSuccess(formData);
      }
      
      // Reset form variables on success
      setFormData({
        name: '',
        purpose: '',
        hostEmail: '',
        photo: '',
      });
      setCapturedImage(null);
    } catch (err) {
      setSubmitError('Failed to complete registration. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>Visitor Registration</h2>
        <p style={styles.subtitle}>
          Please register your details and take a quick photo for visitor pass verification.
        </p>

        {submitSuccess && (
          <div style={styles.successBadge}>
            <span style={styles.successIcon}>✓</span>
            <div>
              <strong>Registration Sent!</strong>
              <p style={{ margin: '4px 0 0 0', fontSize: '13px' }}>
                Your request has been routed to the host for immediate approval.
              </p>
            </div>
          </div>
        )}

        {submitError && (
          <div style={styles.errorAlert}>
            <span>⚠️</span>
            <p style={{ margin: 0, paddingLeft: '8px' }}>{submitError}</p>
          </div>
        )}

        {/* WebCam Capture View Panel (Modal overlay inside card context) */}
        {showWebcam && (
          <div style={styles.webcamModal}>
            <div style={styles.webcamHeader}>
              <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 'bold' }}>Capture Face Photo</h3>
              <button 
                onClick={() => { stopCamera(); setShowWebcam(false); }} 
                style={styles.closeBtn}
                title="Close Camera"
              >
                ✕
              </button>
            </div>

            <div style={styles.webcamFeedContainer}>
              {cameraError ? (
                <div style={styles.cameraErrorBox}>
                  <p style={{ fontSize: '24px', margin: '0 0 8px 0' }}>📷</p>
                  <p style={{ margin: 0, fontSize: '13px', lineHeight: '1.4' }}>{cameraError}</p>
                  <button onClick={() => startCamera(selectedDeviceId)} style={styles.retryBtn}>
                    Try Again
                  </button>
                </div>
              ) : capturedImage ? (
                <div style={styles.aspectRatioWrapper}>
                  <img src={capturedImage} alt="Captured preview" style={styles.webcamPreview} />
                  <div style={styles.photoOverlayText}>Captured snapshot</div>
                </div>
              ) : (
                <div style={styles.aspectRatioWrapper}>
                  {isCameraLoading && (
                    <div style={styles.loaderOverlay}>
                      <div style={styles.loaderSpinner}></div>
                      <p style={{ fontSize: '12px', marginTop: '8px', color: '#666' }}>Initializing stream...</p>
                    </div>
                  )}
                  <video 
                    ref={videoRef} 
                    autoPlay 
                    playsInline 
                    style={styles.webcamVideo} 
                  />
                  {/* Subtle target alignment frame overlay */}
                  <div style={styles.alignmentGuide}>
                    <div style={styles.faceEllipse}></div>
                  </div>
                </div>
              )}
            </div>

            {/* Camera Switch Controls */}
            {!cameraError && !capturedImage && videoDevices.length > 1 && (
              <div style={styles.cameraControlPanel}>
                <label htmlFor="cameraSelect" style={{ fontSize: '12px', fontWeight: 'bold', color: '#555' }}>
                  Camera Source:
                </label>
                <select 
                  id="cameraSelect"
                  value={selectedDeviceId} 
                  onChange={handleDeviceChange}
                  style={styles.deviceSelect}
                >
                  {videoDevices.map((dev, idx) => (
                    <option key={dev.deviceId} value={dev.deviceId}>
                      {dev.label || `Camera ${idx + 1}`}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Action buttons footer */}
            <div style={styles.webcamFooter}>
              {capturedImage ? (
                <>
                  <button onClick={retakePhoto} style={styles.cancelBtn}>
                    Retake Photo
                  </button>
                  <button onClick={acceptPhoto} style={styles.confirmBtn}>
                    Apply Photo
                  </button>
                </>
              ) : (
                <>
                  <button 
                    onClick={() => { stopCamera(); setShowWebcam(false); }} 
                    style={styles.cancelBtn}
                  >
                    Cancel
                  </button>
                  <button 
                    onClick={captureSnapshot} 
                    disabled={!!cameraError || isCameraLoading} 
                    style={{
                      ...styles.captureBtn,
                      opacity: (cameraError || isCameraLoading) ? 0.6 : 1,
                      cursor: (cameraError || isCameraLoading) ? 'not-allowed' : 'pointer',
                    }}
                  >
                    <span>📸</span> Capture Snapshot
                  </button>
                </>
              )}
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate style={styles.form}>
          {/* Avatar Photo Profile Attachment Element */}
          <div style={styles.photoUploadRow}>
            <div style={styles.photoContainer}>
              {formData.photo ? (
                <div style={styles.avatarWrapper}>
                  <img src={formData.photo} alt="Visitor snapshot" style={styles.avatarImage} />
                  <button 
                    type="button" 
                    onClick={() => setFormData(prev => ({ ...prev, photo: '' }))} 
                    style={styles.removePhotoBtn}
                    title="Remove Photo"
                  >
                    ✕
                  </button>
                </div>
              ) : (
                <div 
                  onClick={() => { setShowWebcam(true); startCamera(); }} 
                  style={styles.avatarPlaceholder}
                  title="Click to take snapshot"
                >
                  <span style={styles.cameraIconBadge}>📷</span>
                </div>
              )}
            </div>
            <div style={styles.photoMeta}>
              <h4 style={{ margin: '0 0 4px 0', fontSize: '14px', fontWeight: 'bold', color: '#333' }}>
                Visitor Photo
              </h4>
              <p style={{ margin: 0, fontSize: '12px', color: '#666', lineHeight: '1.4' }}>
                {formData.photo 
                  ? "✓ Photo attached successfully. Ready for pass issuance." 
                  : "A verification snapshot is recommended for credential badges."
                }
              </p>
              {!formData.photo && (
                <button
                  type="button"
                  onClick={() => { setShowWebcam(true); startCamera(); }}
                  style={styles.openWebcamBtn}
                >
                  <span>📷</span> Launch Webcam Camera
                </button>
              )}
            </div>
          </div>
          {errors.photo && <p style={{ ...styles.errorMessage, marginTop: '-12px' }}>{errors.photo}</p>}

          {/* Visitor's Full Name Input Field */}
          <div style={styles.formGroup}>
            <label htmlFor="name" style={styles.label}>
              Full Name <span style={styles.required}>*</span>
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="e.g. Jane Doe"
              style={{
                ...styles.input,
                borderColor: errors.name ? '#d32f2f' : '#ccc',
              }}
              disabled={isSubmitting}
              required
            />
            {errors.name && <p style={styles.errorMessage}>{errors.name}</p>}
          </div>

          {/* Purpose of Visit Dropdown Field */}
          <div style={styles.formGroup}>
            <label htmlFor="purpose" style={styles.label}>
              Purpose of Visit <span style={styles.required}>*</span>
            </label>
            <select
              id="purpose"
              name="purpose"
              value={formData.purpose}
              onChange={handleChange}
              style={{
                ...styles.select,
                borderColor: errors.purpose ? '#d32f2f' : '#ccc',
              }}
              disabled={isSubmitting}
              required
            >
              {purposeOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            {errors.purpose && <p style={styles.errorMessage}>{errors.purpose}</p>}
          </div>

          {/* Host Employee's Email Input Field */}
          <div style={styles.formGroup}>
            <label htmlFor="hostEmail" style={styles.label}>
              Host Email <span style={styles.required}>*</span>
            </label>
            <input
              type="email"
              id="hostEmail"
              name="hostEmail"
              value={formData.hostEmail}
              onChange={handleChange}
              placeholder="host@company.com"
              style={{
                ...styles.input,
                borderColor: errors.hostEmail ? '#d32f2f' : '#ccc',
              }}
              disabled={isSubmitting}
              required
            />
            {errors.hostEmail && <p style={styles.errorMessage}>{errors.hostEmail}</p>}
          </div>

          {/* Form Action Buttons */}
          <button
            type="submit"
            style={{
              ...styles.submitBtn,
              opacity: isSubmitting ? 0.7 : 1,
              cursor: isSubmitting ? 'not-allowed' : 'pointer',
            }}
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Processing Registration...' : 'Register Visit'}
          </button>
        </form>
      </div>
    </div>
  );
};

// Clean default inline css layout styles
const styles = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100%',
    padding: '24px',
    backgroundColor: '#f5f7fb',
    fontFamily: 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  },
  card: {
    backgroundColor: '#ffffff',
    borderRadius: '12px',
    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.08)',
    width: '100%',
    maxWidth: '480px',
    padding: '32px',
    boxSizing: 'border-box',
    position: 'relative',
    overflow: 'hidden',
  },
  title: {
    margin: '0 0 8px 0',
    fontSize: '24px',
    fontWeight: '700',
    color: '#1a237e',
    textAlign: 'center',
  },
  subtitle: {
    margin: '0 0 24px 0',
    fontSize: '14px',
    color: '#666',
    textAlign: 'center',
    lineHeight: '1.5',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },
  label: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#333333',
  },
  required: {
    color: '#d32f2f',
  },
  input: {
    padding: '12px 16px',
    fontSize: '15px',
    color: '#333',
    backgroundColor: '#ffffff',
    border: '1px solid #ccc',
    borderRadius: '8px',
    transition: 'border-color 0.15s ease-in-out',
    outline: 'none',
  },
  select: {
    padding: '12px 16px',
    fontSize: '15px',
    color: '#333',
    backgroundColor: '#ffffff',
    border: '1px solid #ccc',
    borderRadius: '8px',
    transition: 'border-color 0.15s ease-in-out',
    outline: 'none',
    WebkitAppearance: 'none',
    appearance: 'none',
    backgroundImage: `url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23333' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e")`,
    backgroundRepeat: 'no-repeat',
    backgroundPosition: 'right 16px center',
    backgroundSize: '16px',
    paddingRight: '40px',
  },
  errorMessage: {
    margin: '4px 0 0 0',
    fontSize: '12px',
    color: '#d32f2f',
    fontWeight: '500',
  },
  submitBtn: {
    backgroundColor: '#1a237e',
    color: '#ffffff',
    border: 'none',
    borderRadius: '8px',
    padding: '14px',
    fontSize: '16px',
    fontWeight: '600',
    transition: 'background-color 0.2s ease',
    marginTop: '8px',
  },
  successBadge: {
    display: 'flex',
    alignItems: 'start',
    backgroundColor: '#e8f5e9',
    border: '1px solid #c8e6c9',
    borderRadius: '8px',
    padding: '16px',
    marginBottom: '20px',
    color: '#2e7d32',
    lineHeight: '1.4',
  },
  successIcon: {
    fontSize: '20px',
    marginRight: '12px',
    lineHeight: '1',
  },
  errorAlert: {
    display: 'flex',
    alignItems: 'center',
    backgroundColor: '#ffebee',
    border: '1px solid #ffcdd2',
    borderRadius: '8px',
    padding: '12px 16px',
    marginBottom: '20px',
    color: '#c62828',
    fontSize: '14px',
  },

  /* Photo Section Elements */
  photoUploadRow: {
    display: 'flex',
    gap: '16px',
    alignItems: 'center',
    backgroundColor: '#f8fafc',
    padding: '16px',
    borderRadius: '8px',
    border: '1px dashed #cbd5e1',
  },
  photoContainer: {
    flexShrink: 0,
  },
  avatarPlaceholder: {
    width: '64px',
    height: '64px',
    borderRadius: '50%',
    backgroundColor: '#e2e8f0',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    cursor: 'pointer',
    border: '2px solid #cbd5e1',
    transition: 'all 0.2s ease',
  },
  cameraIconBadge: {
    fontSize: '24px',
  },
  avatarWrapper: {
    position: 'relative',
    width: '64px',
    height: '64px',
  },
  avatarImage: {
    width: '64px',
    height: '64px',
    borderRadius: '50%',
    objectFit: 'cover',
    border: '2px solid #1a237e',
  },
  removePhotoBtn: {
    position: 'absolute',
    top: '-4px',
    right: '-4px',
    width: '18px',
    height: '18px',
    borderRadius: '50%',
    backgroundColor: '#ef4444',
    color: '#fff',
    border: 'none',
    fontSize: '10px',
    fontWeight: 'bold',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  photoMeta: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'start',
  },
  openWebcamBtn: {
    backgroundColor: 'transparent',
    border: 'none',
    color: '#1a237e',
    fontSize: '13px',
    fontWeight: '600',
    padding: '4px 0',
    margin: '6px 0 0 0',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
  },

  /* Webcam Screen Capture UI Overlay */
  webcamModal: {
    position: 'absolute',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    backgroundColor: '#ffffff',
    zIndex: 100,
    display: 'flex',
    flexDirection: 'column',
    boxSizing: 'border-box',
    animation: 'slideUp 0.3s ease-out',
  },
  webcamHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '16px 24px',
    borderBottom: '1px solid #e2e8f0',
  },
  closeBtn: {
    border: 'none',
    background: 'none',
    fontSize: '18px',
    cursor: 'pointer',
    color: '#666',
  },
  webcamFeedContainer: {
    flex: 1,
    backgroundColor: '#000000',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
    overflow: 'hidden',
  },
  aspectRatioWrapper: {
    width: '100%',
    height: '100%',
    maxWidth: '480px',
    position: 'relative',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  webcamVideo: {
    width: '100%',
    height: '100%',
    objectFit: 'cover',
    transform: 'scaleX(-1)', // Mirrored for natural user visual sync
  },
  webcamPreview: {
    width: '100%',
    height: '100%',
    objectFit: 'cover',
  },
  alignmentGuide: {
    position: 'absolute',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    pointerEvents: 'none',
  },
  faceEllipse: {
    width: '180px',
    height: '240px',
    border: '2px dashed rgba(255, 255, 255, 0.7)',
    borderRadius: '50%',
    boxShadow: '0 0 0 999px rgba(0, 0, 0, 0.4)', // Dim outer bounds
  },
  cameraControlPanel: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '10px 24px',
    backgroundColor: '#f8fafc',
    borderBottom: '1px solid #e2e8f0',
    gap: '12px',
  },
  deviceSelect: {
    flex: 1,
    padding: '6px 12px',
    fontSize: '13px',
    border: '1px solid #cbd5e1',
    borderRadius: '6px',
    backgroundColor: '#fff',
    outline: 'none',
  },
  webcamFooter: {
    display: 'flex',
    gap: '12px',
    padding: '16px 24px',
    borderTop: '1px solid #e2e8f0',
    justifyContent: 'flex-end',
    backgroundColor: '#fff',
  },
  captureBtn: {
    backgroundColor: '#1a237e',
    color: '#fff',
    border: 'none',
    borderRadius: '6px',
    padding: '10px 20px',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
  },
  cancelBtn: {
    backgroundColor: '#fff',
    color: '#334155',
    border: '1px solid #cbd5e1',
    borderRadius: '6px',
    padding: '10px 20px',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  confirmBtn: {
    backgroundColor: '#16a34a',
    color: '#fff',
    border: 'none',
    borderRadius: '6px',
    padding: '10px 20px',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  cameraErrorBox: {
    color: '#fff',
    textAlign: 'center',
    padding: '24px',
    maxWidth: '280px',
  },
  retryBtn: {
    marginTop: '16px',
    backgroundColor: '#ef4444',
    color: '#fff',
    border: 'none',
    borderRadius: '6px',
    padding: '8px 16px',
    fontSize: '13px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  photoOverlayText: {
    position: 'absolute',
    bottom: '16px',
    backgroundColor: 'rgba(0,0,0,0.6)',
    color: '#fff',
    padding: '4px 12px',
    borderRadius: '12px',
    fontSize: '12px',
  },
  loaderOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    backgroundColor: '#f1f5f9',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 2,
  },
  loaderSpinner: {
    width: '28px',
    height: '28px',
    border: '3px solid #cbd5e1',
    borderTopColor: '#1a237e',
    borderRadius: '50%',
    animation: 'spin 0.8s linear infinite',
  },
};

export default VisitorRegistrationForm;
