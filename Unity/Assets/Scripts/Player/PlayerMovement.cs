using UnityEngine;

/// <summary>
/// Basic third-person movement foundation for the Bangladesh Life Simulator.
/// Connect MoveInput to a mobile joystick in the Unity scene.
/// </summary>
[RequireComponent(typeof(CharacterController))]
public sealed class PlayerMovement : MonoBehaviour
{
    [SerializeField] private float walkSpeed = 2.0f;
    [SerializeField] private float runSpeed = 4.2f;
    [SerializeField] private float jumpHeight = 1.1f;
    [SerializeField] private float gravity = -18f;
    [SerializeField] private Transform cameraTransform;
    [SerializeField] private float rotationSpeed = 12f;

    public Vector2 MoveInput { get; set; }
    public bool RunHeld { get; set; }
    public bool JumpRequested { get; set; }

    private CharacterController controller;
    private float verticalVelocity;

    private void Awake()
    {
        controller = GetComponent<CharacterController>();
        if (cameraTransform == null && Camera.main != null)
            cameraTransform = Camera.main.transform;
    }

    private void Update()
    {
        Vector3 forward = cameraTransform != null
            ? Vector3.ProjectOnPlane(cameraTransform.forward, Vector3.up).normalized
            : transform.forward;
        Vector3 right = cameraTransform != null
            ? Vector3.ProjectOnPlane(cameraTransform.right, Vector3.up).normalized
            : transform.right;

        Vector3 direction = Vector3.ClampMagnitude(
            forward * MoveInput.y + right * MoveInput.x, 1f);

        if (direction.sqrMagnitude > 0.001f)
        {
            Quaternion target = Quaternion.LookRotation(direction, Vector3.up);
            transform.rotation = Quaternion.Slerp(
                transform.rotation, target, rotationSpeed * Time.deltaTime);
        }

        if (controller.isGrounded && verticalVelocity < 0f)
            verticalVelocity = -2f;

        if (JumpRequested && controller.isGrounded)
            verticalVelocity = Mathf.Sqrt(jumpHeight * -2f * gravity);

        JumpRequested = false;
        verticalVelocity += gravity * Time.deltaTime;

        float speed = RunHeld ? runSpeed : walkSpeed;
        Vector3 velocity = direction * speed;
        velocity.y = verticalVelocity;
        controller.Move(velocity * Time.deltaTime);
    }
}
