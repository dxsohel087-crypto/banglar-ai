using UnityEngine;

namespace BangladeshLifeSimulator
{
    [RequireComponent(typeof(CharacterController))]
    public sealed class PlayerMovementController : MonoBehaviour
    {
        [Header("Input")]
        [SerializeField] private SimpleMobileJoystick joystick;
        [SerializeField] private Transform cameraTransform;

        [Header("Movement")]
        [SerializeField, Min(0f)] private float walkSpeed = 2.2f;
        [SerializeField, Min(0f)] private float runSpeed = 4.5f;
        [SerializeField, Min(0f)] private float rotationSpeed = 12f;
        [SerializeField] private float gravity = -20f;
        [SerializeField, Min(0f)] private float jumpHeight = 1.2f;

        [Header("Animation (optional)")]
        [SerializeField] private Animator animator;
        [SerializeField] private string speedParameter = "Speed";
        [SerializeField] private string groundedParameter = "Grounded";

        private CharacterController characterController;
        private Vector3 verticalVelocity;
        private bool isRunning;

        private void Awake()
        {
            characterController = GetComponent<CharacterController>();
            if (cameraTransform == null && Camera.main != null)
                cameraTransform = Camera.main.transform;
        }

        private void Update()
        {
            Vector2 input = joystick != null ? joystick.Direction : new Vector2(
                Input.GetAxisRaw("Horizontal"), Input.GetAxisRaw("Vertical"));
            input = Vector2.ClampMagnitude(input, 1f);

            Vector3 forward = cameraTransform != null ? cameraTransform.forward : Vector3.forward;
            Vector3 right = cameraTransform != null ? cameraTransform.right : Vector3.right;
            forward.y = 0f;
            right.y = 0f;
            forward.Normalize();
            right.Normalize();

            Vector3 move = forward * input.y + right * input.x;
            float speed = isRunning ? runSpeed : walkSpeed;

            if (move.sqrMagnitude > 0.001f)
            {
                Quaternion targetRotation = Quaternion.LookRotation(move, Vector3.up);
                transform.rotation = Quaternion.Slerp(
                    transform.rotation, targetRotation, rotationSpeed * Time.deltaTime);
            }

            if (characterController.isGrounded && verticalVelocity.y < 0f)
                verticalVelocity.y = -2f;

            verticalVelocity.y += gravity * Time.deltaTime;
            characterController.Move((move * speed + verticalVelocity) * Time.deltaTime);

            if (animator != null)
            {
                animator.SetFloat(speedParameter, move.magnitude * (isRunning ? 2f : 1f));
                animator.SetBool(groundedParameter, characterController.isGrounded);
            }
        }

        public void SetRunning(bool running) => isRunning = running;

        public void Jump()
        {
            if (characterController.isGrounded)
                verticalVelocity.y = Mathf.Sqrt(jumpHeight * -2f * gravity);
        }
    }
}
