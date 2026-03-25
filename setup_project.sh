#!/bin/bash

BASE="app/src/main/java/com/studygenai"

# Create all necessary directories first
mkdir -p "$BASE/ui/screens/otp"
mkdir -p "$BASE/ui/screens/onboarding"
mkdir -p "$BASE/data/firebase"
mkdir -p "$BASE/domain/repository"
mkdir -p "$BASE/domain/usecase/auth"
mkdir -p "$BASE/domain/model"

# Create files
touch "$BASE/data/firebase/EmailJSService.kt"
touch "$BASE/data/repository/AuthRepositoryImpl.kt"
touch "$BASE/domain/repository/AuthRepository.kt"
touch "$BASE/domain/usecase/auth/SignUpUseCase.kt"
touch "$BASE/domain/usecase/auth/SignInUseCase.kt"
touch "$BASE/domain/usecase/auth/SendOtpUseCase.kt"
touch "$BASE/domain/usecase/auth/VerifyOtpUseCase.kt"
touch "$BASE/domain/model/User.kt"
touch "$BASE/ui/screens/otp/OtpScreen.kt"
touch "$BASE/ui/screens/otp/OtpViewModel.kt"
touch "$BASE/ui/screens/onboarding/OnboardingScreen.kt"
touch "$BASE/ui/screens/onboarding/OnboardingViewModel.kt"

echo "✅ Auth files created"